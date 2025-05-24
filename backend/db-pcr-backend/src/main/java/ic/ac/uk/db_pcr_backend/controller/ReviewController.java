package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gerrit.extensions.restapi.RestApiException;

import ic.ac.uk.db_pcr_backend.dto.datadto.ChangeRequestDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.ProjectDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.PseudonymCommentInfoDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.PseudonymGitlabCommitDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.ReviewAssignmentPseudonymDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.UsernameCommentInfoDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInfoDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInputDto;
import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectUserPseudonymEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.RoleType;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabCommitRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;
import ic.ac.uk.db_pcr_backend.service.CommentService;
import ic.ac.uk.db_pcr_backend.service.GerritService;
import ic.ac.uk.db_pcr_backend.service.PseudoNameService;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private GerritService gerritSvc;

    @Autowired
    private PseudoNameService pseudoNameSvc;

    @Autowired
    private CommentService commentSvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private GitlabCommitRepo commitRepo;

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    @Value("${gitlab.group.id}")
    private String groupId;

    /**
     * Return all the Projects this username is a reviewer *for*,
     * based on the assignments table.
     */
    @Transactional(readOnly = true)
    @GetMapping("/get-projects-to-review")
    public ResponseEntity<List<ProjectDto>> getProjectsToReview(
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client,
            @AuthenticationPrincipal OAuth2User oauth2User) throws Exception {

        System.out.println("STAGE: ReviewController.getProjectsToReview");

        Long gitlabUserId = Long.valueOf(oauth2User.getAttribute("id").toString());
        String username = oauth2User.getAttribute("username").toString();

        UserEntity reviewer = userRepo.findByUsername(username)
                .orElseGet(() -> userRepo.save(new UserEntity(gitlabUserId, username, null)));

        List<ReviewAssignmentEntity> assignments = reviewAssignmentRepo.findByReviewer(reviewer);

        List<ProjectDto> groupProjects = assignments.stream()
                .map(ReviewAssignmentEntity::getGroupProject)
                .distinct()
                .map(ProjectDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(groupProjects);
    }

    /**
     * Return commit list for a project author submitted to review
     */
    @Transactional(readOnly = true)
    @GetMapping("/get-review-project-commits")
    public ResponseEntity<List<ChangeRequestDto>> getReviewProjectCommits(
            @RequestParam("assignmentId") String assignmentId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client,
            @AuthenticationPrincipal OAuth2User oauth2User) throws Exception {

        System.out.println("STAGE: ReviewController.getReviewProjectCommits");

        // Find the review assignment
        ReviewAssignmentEntity assignment = reviewAssignmentRepo.findById(Long.valueOf(assignmentId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assignment not found: " + assignmentId));

        // Find the change requests
        List<ChangeRequestDto> changeRequests = changeRequestRepo
                .findByAssignment(assignment)
                .stream()
                .map(ChangeRequestDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(changeRequests);
    }

    /**
     * Get the review assignment pseudonym by personal project id (from author side)
     */
    @Transactional(readOnly = true)
    @GetMapping("/get-author-assignment-pseudonym")
    public ResponseEntity<ReviewAssignmentPseudonymDto[]> getAuthorAssignmentForReviewer(
            @RequestParam("projectId") String projectId,
            @AuthenticationPrincipal OAuth2User oauth2User) throws Exception {

        System.out.println("STAGE: ReviewController.getAuthorAssignmentForReviewer");

        // Find the project
        ProjectEntity personalProejct = projectRepo.findById(Long.valueOf(projectId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + projectId));

        ProjectEntity groupProject = personalProejct.getParentProject();

        UserEntity author = userRepo.findByUsername(oauth2User.getAttribute("username"))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reviewer not found: " + oauth2User.getAttribute("username")));

        // Find the review assignment
        List<ReviewAssignmentEntity> assignments = reviewAssignmentRepo
                .findByAuthorAndGroupProject(author, groupProject);

        ReviewAssignmentPseudonymDto[] dtoArray = assignments.stream()
                .map(asn -> {
                    var authorMask = pseudoNameSvc.getPseudonymInReviewAssignment(asn, RoleType.AUTHOR);
                    var reviewerMask = pseudoNameSvc.getPseudonymInReviewAssignment(asn, RoleType.REVIEWER);
                    return new ReviewAssignmentPseudonymDto(asn, authorMask, reviewerMask);
                })
                .toArray(ReviewAssignmentPseudonymDto[]::new);

        return ResponseEntity.ok(dtoArray);
    }

    /**
     * Get the review assignment pseudonym by group project id
     */
    @Transactional(readOnly = true)
    @GetMapping("/get-review-assignment-pseudonym")
    public ResponseEntity<ReviewAssignmentPseudonymDto[]> getReviewAssignmentForReviewer(
            @RequestParam("groupProjectId") String groupProjectId,
            @AuthenticationPrincipal OAuth2User oauth2User) throws Exception {

        System.out.println("STAGE: ReviewController.getReviewAssignmentForReviewer");

        // Find the project
        ProjectEntity groupProject = projectRepo.findById(Long.valueOf(groupProjectId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + groupProjectId));

        UserEntity reviewer = userRepo.findByUsername(oauth2User.getAttribute("username"))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reviewer not found: " + oauth2User.getAttribute("username")));

        // Find the review assignment
        List<ReviewAssignmentEntity> assignments = reviewAssignmentRepo
                .findByReviewerAndGroupProject(reviewer, groupProject);

        ReviewAssignmentPseudonymDto[] dtoArray = assignments.stream()
                .map(asn -> {
                    var authorMask = pseudoNameSvc.getPseudonymInReviewAssignment(asn, RoleType.AUTHOR);
                    var reviewerMask = pseudoNameSvc.getPseudonymInReviewAssignment(asn, RoleType.REVIEWER);
                    return new ReviewAssignmentPseudonymDto(asn, authorMask, reviewerMask);
                })
                .toArray(ReviewAssignmentPseudonymDto[]::new);

        return ResponseEntity.ok(dtoArray);
    }

    /**
     * Get author pseudonym and commit dto for a gerrit change id
     */
    @Transactional(readOnly = true)
    @GetMapping("/get-author-pseudonym-commit")
    public ResponseEntity<PseudonymGitlabCommitDto> getAuthorPseudonymCommitForChangeId(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @AuthenticationPrincipal OAuth2User oauth2User) throws Exception {

        System.out.println("STAGE: ReviewController.getAuthorPseudonymCommitForChangeId");

        // Find the change request
        List<ChangeRequestEntity> changeRequestList = changeRequestRepo
                .findByGerritChangeId(gerritChangeId);

        if (changeRequestList.isEmpty()) {
            throw new IllegalArgumentException("Change request not found: " + gerritChangeId);
        }

        // Will find multiple change request, but they all have the same author
        ChangeRequestEntity changeRequest = changeRequestList.get(0);

        // Find the commit
        GitlabCommitEntity commit = changeRequest.getCommit();

        // Find the review assignment
        ReviewAssignmentEntity assignment = changeRequest.getAssignment();

        ProjectUserPseudonymEntity authorMask = pseudoNameSvc.getPseudonymInReviewAssignment(assignment,
                RoleType.AUTHOR);

        // Dto
        PseudonymGitlabCommitDto commitDto = new PseudonymGitlabCommitDto(
                commit, authorMask.getPseudonym().getName());

        return ResponseEntity.ok(commitDto);
    }

    /** Get list of changed file names in a gerrit change */
    @GetMapping("/get-changed-files")
    public ResponseEntity<List<String>> getChangedFiles(
            @RequestParam("gerritChangeId") String gerritChangeId) throws Exception {

        System.out.println("STAGE: ReviewController.getChangedFiles");

        return ResponseEntity.ok(gerritSvc.getChangedFileNames(gerritChangeId));
    }

    /** Get changed files content */
    @GetMapping("/get-changed-files-content")
    public ResponseEntity<Map<String, String[]>> getChangedFilesContent(
            @RequestParam("gerritChangeId") String gerritChangeId) throws Exception {

        System.out.println("STAGE: ReviewController.getChangedFilesContent");

        return ResponseEntity.ok(gerritSvc.getChangedFileContent(gerritChangeId));
    }

    /** Get Gerrit ChangeDiff via Uuid and ChangeId */
    @GetMapping("/get-change-diff")
    public String getChangeDiff(@RequestParam("gerritChangeId") String gerritChangeId) throws Exception {

        System.out.println("STAGE: ReviewController.getChangeDiff");

        return gerritSvc.fetchRawPatch(gerritChangeId, "current");
    }

    /** Push some gitlab commits to gerrit */
    @PostMapping("/post-request-review")
    public ResponseEntity<Map<String, String>> requestReview(
            @RequestParam("projectId") String projectId,
            @RequestParam("sha") String gitlabCommitId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
            throws Exception {

        System.out.println("STAGE: ReviewController.requestReview");

        // 1) Fetch the GitLab OAuth token for this user/session
        String accessToken = client.getAccessToken().getTokenValue();

        // Find project
        ProjectEntity project = projectRepo.findById(Long.valueOf(projectId))
                .orElseThrow(() -> new IllegalArgumentException("Unknown project id " + projectId));

        String gitlabProjectId = String.valueOf(project.getGitlabProjectId());
        String username = project.getOwner().getUsername();

        // Find Commit
        GitlabCommitEntity commit = commitRepo.findByGitlabCommitId(gitlabCommitId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown commit id " + gitlabCommitId));

        // 2) Delegate to the service
        String gerritChangeId = gerritSvc.submitForReview(
                gitlabProjectId, commit, accessToken, username);

        // 3) Return the new Change number to the frontend
        return ResponseEntity.ok(Map.of("changeId", gerritChangeId));
    }

    /* ---------- COMMENTING -------------- */

    @GetMapping("/get-gerrit-change-comments")
    public ResponseEntity<List<CommentInfoDto>> getGerritChangeComments(
            @RequestParam("gerritChangeId") String gerritChangeId) throws RestApiException {

        System.out.println("STAGE: ReviewController.getGerritChangeComments");

        return ResponseEntity.ok(gerritSvc.getGerritChangeComments(gerritChangeId));
    }

    @GetMapping("/get-gerrit-change-comments-with-pseudonym")
    public ResponseEntity<List<PseudonymCommentInfoDto>> getGerritChangeCommentsWithPseudonym(
            @RequestParam("gerritChangeId") String gerritChangeId) throws RestApiException, GitLabApiException {

        System.out.println("STAGE: ReviewController.getGerritChangeCommentsWithPseudonyms");

        List<CommentInfoDto> comments = gerritSvc.getGerritChangeComments(gerritChangeId);
        List<PseudonymCommentInfoDto> pseudonymComments = commentSvc.getCommentsWithPseudonym(gerritChangeId, comments);

        return ResponseEntity.ok(pseudonymComments);
    }

    @GetMapping("/get-gerrit-change-comments-with-username")
    public ResponseEntity<List<UsernameCommentInfoDto>> getGerritChangeCommentsWithUsername(
            @RequestParam("gerritChangeId") String gerritChangeId) throws RestApiException, GitLabApiException {

        System.out.println("STAGE: ReviewController.getGerritChangeCommentsWithUsername");

        List<CommentInfoDto> comments = gerritSvc.getGerritChangeComments(gerritChangeId);
        List<UsernameCommentInfoDto> usernameComments = commentSvc.getCommentsWithUsername(gerritChangeId, comments);

        return ResponseEntity.ok(usernameComments);
    }

    @GetMapping("/get-gerrit-change-draft-comments")
    public ResponseEntity<List<CommentInfoDto>> getGerritChangeDraftComments(
            @RequestParam("gerritChangeId") String gerritChangeId) throws RestApiException {

        System.out.println("STAGE: ReviewController.getGerritChangeDraftComments");

        return ResponseEntity.ok(gerritSvc.getGerritChangeDraftComments(gerritChangeId));
    }

    @PostMapping("/post-gerrit-draft-comment")
    public ResponseEntity<CommentInfoDto> postGerritDraftComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestBody CommentInputDto commentInput) throws RestApiException {

        System.out.println("STAGE: ReviewController.postGerritDraftComment");

        return ResponseEntity.ok(gerritSvc.postGerritDraft(
                gerritChangeId, commentInput));
    }

    @PutMapping("/update-gerrit-draft-comment")
    public ResponseEntity<CommentInfoDto> updateGerritDraftComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestBody CommentInputDto commentInput) throws RestApiException {

        System.out.println("STAGE: ReviewController.updateGerritDraftComment");

        return ResponseEntity.ok(gerritSvc.updateGerritDraft(
                gerritChangeId, commentInput));
    }

    @DeleteMapping("/delete-gerrit-draft-comment")
    public ResponseEntity<Void> deleteGerritDraftComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestBody CommentInputDto commentInput) throws RestApiException {

        System.out.println("STAGE: ReviewController.deleteGerritDraftComment");

        gerritSvc.deleteGerritDraft(gerritChangeId, commentInput);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/publish-gerrit-draft-comments")
    public ResponseEntity<Void> publishDraftComments(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestParam("assignmentId") String assignmentId,
            @RequestBody List<String> draftIds,
            @AuthenticationPrincipal OAuth2User oauth2User) throws Exception {

        System.out.println("STAGE: ReviewController.publishDraftComments");

        gerritSvc.publishDrafts(gerritChangeId, draftIds);

        // Add commentEntity to database
        String username = oauth2User.getAttribute("username").toString();
        commentSvc.recordCommentsForChangeId(gerritChangeId, assignmentId, draftIds, username);

        return ResponseEntity.noContent().build();
    }

}
