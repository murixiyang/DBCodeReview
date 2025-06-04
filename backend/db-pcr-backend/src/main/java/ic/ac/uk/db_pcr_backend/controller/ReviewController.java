package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Autowired;
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
import ic.ac.uk.db_pcr_backend.dto.datadto.NameCommentInfoDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.ProjectDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.PseudonymGitlabCommitDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.ReviewAssignmentPseudonymDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInfoDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInputDto;
import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectUserPseudonymEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.ReactState;
import ic.ac.uk.db_pcr_backend.model.RoleType;
import ic.ac.uk.db_pcr_backend.redactor.Redactor;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GerritCommentRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabCommitRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;
import ic.ac.uk.db_pcr_backend.service.CommentService;
import ic.ac.uk.db_pcr_backend.service.GerritService;
import ic.ac.uk.db_pcr_backend.service.NotificationService;
import ic.ac.uk.db_pcr_backend.service.PseudoNameService;
import ic.ac.uk.db_pcr_backend.service.RedactionService;
import ic.ac.uk.db_pcr_backend.service.ReviewStatusService;
import ic.ac.uk.db_pcr_backend.service.UserService;

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
    private ReviewStatusService reviewStatusSvc;

    @Autowired
    private NotificationService notificationSvc;

    @Autowired
    private RedactionService redactSvc;

    @Autowired
    private UserService userSvc;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private GitlabCommitRepo commitRepo;

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    @Autowired
    private GerritCommentRepo gerritCommentRepo;

    /**
     * Return all the Projects this username is a reviewer *for*,
     * based on the assignments table.
     */
    @Transactional
    @GetMapping("/get-projects-to-review")
    public ResponseEntity<List<ProjectDto>> getProjectsToReview(
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client,
            @AuthenticationPrincipal OAuth2User oauth2User) throws Exception {

        System.out.println("STAGE: ReviewController.getProjectsToReview");

        Long gitlabUserId = Long.valueOf(oauth2User.getAttribute("id").toString());
        String username = oauth2User.getAttribute("username").toString();

        UserEntity reviewer = userSvc.getOrCreateUserByName(gitlabUserId, username);

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

        // Find blockNames for redaction
        String username = oauth2User.getAttribute("username").toString();
        List<String> blockNames = redactSvc.buildAllUsernames(username);

        // Find the change requests
        List<ChangeRequestDto> changeRequests = changeRequestRepo
                .findByAssignment(assignment)
                .stream()
                .map(cr -> ChangeRequestDto.fromEntity(cr, blockNames))
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

        UserEntity author = userSvc.getOrExceptionUserByName(oauth2User.getAttribute("username"));

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

        UserEntity reviewer = userSvc.getOrExceptionUserByName(oauth2User.getAttribute("username"));

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

        String username = oauth2User.getAttribute("username").toString();
        List<String> redactedFields = redactSvc.buildAllUsernames(username);

        // Dto
        PseudonymGitlabCommitDto commitDto = new PseudonymGitlabCommitDto(
                commit, authorMask.getPseudonym().getName(), redactedFields);

        return ResponseEntity.ok(commitDto);
    }

    /** Get group project id by assignment id */
    @GetMapping("/get-group-project-id-by-assignment")
    public ResponseEntity<Long> getGroupProjectIdByAssignment(
            @RequestParam("assignmentId") String assignmentId) throws Exception {

        System.out.println("STAGE: ReviewController.getGroupProjectIdByAssignment");

        // Find the review assignment
        ReviewAssignmentEntity assignment = reviewAssignmentRepo.findById(Long.valueOf(assignmentId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assignment not found: " + assignmentId));

        // Return the group project id
        return ResponseEntity.ok(assignment.getGroupProject().getId());
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
            @RequestParam("gerritChangeId") String gerritChangeId, @AuthenticationPrincipal OAuth2User oauth2User)
            throws Exception {

        System.out.println("STAGE: ReviewController.getChangedFilesContent");

        Map<String, String[]> changedFileMap = gerritSvc.getChangedFileContent(gerritChangeId);

        // Get the redaction list
        String username = oauth2User.getAttribute("username").toString();
        List<String> blockNames = redactSvc.buildAllUsernames(username);

        changedFileMap.replaceAll((file, pair) -> new String[] {
                Redactor.redact(pair[0], blockNames),
                Redactor.redact(pair[1], blockNames)
        });

        return ResponseEntity.ok(changedFileMap);
    }

    /** Get changed files content compared to a specific version */
    @GetMapping("/get-changed-files-content-compare-to")
    public ResponseEntity<Map<String, String[]>> getChangedFilesContentCompareTo(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestParam("compareToChangeId") String compareToChangeId,
            @AuthenticationPrincipal OAuth2User oauth2User)
            throws Exception {

        System.out.println("STAGE: ReviewController.getChangedFilesContentCompareTo");

        Map<String, String[]> changedFileMap = gerritSvc.getChangedFileContentCompareTo(gerritChangeId,
                compareToChangeId);

        // Get the redaction list
        String username = oauth2User.getAttribute("username").toString();
        List<String> blockNames = redactSvc.buildAllUsernames(username);

        changedFileMap.replaceAll((file, pair) -> new String[] {
                Redactor.redact(pair[0], blockNames),
                Redactor.redact(pair[1], blockNames)
        });

        return ResponseEntity.ok(changedFileMap);
    }

    /** Get Gerrit ChangeDiff via Uuid and ChangeId */
    @GetMapping("/get-change-diff")
    public String getChangeDiff(@RequestParam("gerritChangeId") String gerritChangeId,
            @AuthenticationPrincipal OAuth2User oauth2User) throws Exception {

        System.out.println("STAGE: ReviewController.getChangeDiff");

        String rawPatch = gerritSvc.fetchRawPatch(gerritChangeId, "current");

        String username = oauth2User.getAttribute("username").toString();
        List<String> blockNames = redactSvc.buildByGerritChangeId(gerritChangeId, username);

        return Redactor.redact(rawPatch, blockNames);
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

        // Submit review request to Gerrit
        String gerritChangeId = gerritSvc.submitForReview(
                gitlabProjectId, commit, accessToken, username);

        // Have Sent notification to reviewers when creating change request

        // Return the new Change number to the frontend
        return ResponseEntity.ok(Map.of("changeId", gerritChangeId));
    }

    /* ---------- COMMENTING -------------- */

    @GetMapping("/get-gerrit-change-comments")
    public ResponseEntity<List<CommentInfoDto>> getGerritChangeComments(
            @RequestParam("gerritChangeId") String gerritChangeId, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException {

        System.out.println("STAGE: ReviewController.getGerritChangeComments");
        String username = oauth2User.getAttribute("username").toString();

        return ResponseEntity.ok(gerritSvc.getGerritChangeComments(gerritChangeId, username));
    }

    @GetMapping("/get-gerrit-change-comments-with-pseudonym")
    public ResponseEntity<List<NameCommentInfoDto>> getGerritChangeCommentsWithPseudonym(
            @RequestParam("gerritChangeId") String gerritChangeId, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException, GitLabApiException {

        System.out.println("STAGE: ReviewController.getGerritChangeCommentsWithPseudonyms");
        ;
        String username = oauth2User.getAttribute("username").toString();

        List<CommentInfoDto> comments = gerritSvc.getGerritChangeComments(gerritChangeId, username);
        List<CommentInfoDto> commentsWithThumb = commentSvc.addThumbStateToDto(gerritChangeId, comments);
        List<NameCommentInfoDto> pseudonymComments = commentSvc.getCommentsWithPseudonym(gerritChangeId,
                commentsWithThumb);

        return ResponseEntity.ok(pseudonymComments);
    }

    @GetMapping("/get-gerrit-change-comments-with-username")
    public ResponseEntity<List<NameCommentInfoDto>> getGerritChangeCommentsWithUsername(
            @RequestParam("gerritChangeId") String gerritChangeId, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException, GitLabApiException {

        System.out.println("STAGE: ReviewController.getGerritChangeCommentsWithUsername");

        ;
        String username = oauth2User.getAttribute("username").toString();

        List<CommentInfoDto> comments = gerritSvc.getGerritChangeComments(gerritChangeId, username);
        List<CommentInfoDto> commentsWithThumb = commentSvc.addThumbStateToDto(gerritChangeId, comments);
        List<NameCommentInfoDto> usernameComments = commentSvc.getCommentsWithUsername(gerritChangeId,
                commentsWithThumb);

        return ResponseEntity.ok(usernameComments);
    }

    @GetMapping("/get-gerrit-change-draft-comments")
    public ResponseEntity<List<CommentInfoDto>> getGerritChangeDraftComments(
            @RequestParam("gerritChangeId") String gerritChangeId, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException {

        System.out.println("STAGE: ReviewController.getGerritChangeDraftComments");

        ;
        String username = oauth2User.getAttribute("username").toString();

        return ResponseEntity.ok(gerritSvc.getGerritChangeDraftComments(gerritChangeId, username));
    }

    /** Fetch draft comment for specific user only (should not see other's draft) */
    @GetMapping("/get-user-gerrit-change-draft-comments")
    public ResponseEntity<List<CommentInfoDto>> getGerritChangeDraftCommentsForUser(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @AuthenticationPrincipal OAuth2User oauth2User) throws RestApiException {

        System.out.println("STAGE: ReviewController.getGerritChangeDraftCommentsForUser");

        String username = oauth2User.getAttribute("username").toString();

        List<CommentInfoDto> drafts = gerritSvc.getGerritChangeDraftComments(gerritChangeId, username);

        List<CommentInfoDto> filtered = drafts.stream()
                .filter(dto -> {
                    return gerritCommentRepo
                            .findByGerritChangeIdAndGerritCommentId(gerritChangeId, dto.getId())
                            .map(entity -> entity.getCommentUser().getUsername().equals(username))
                            .orElse(false);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    /** Post a reviewer's draft comment to gerrit and record in database */
    @PostMapping("/post-reviewer-gerrit-draft-comment")
    public ResponseEntity<CommentInfoDto> postReviewerGerritDraftComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestParam("assignmentId") String assignmentId,
            @RequestBody CommentInputDto commentInput, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException, GitLabApiException {

        System.out.println("STAGE: ReviewController.postGerritDraftComment");

        String username = oauth2User.getAttribute("username").toString();

        CommentInfoDto savedDraft = gerritSvc.postGerritDraft(gerritChangeId, commentInput, username);

        // Save commentEntity to database
        commentSvc.recordReviewerDraftComment(gerritChangeId, assignmentId, savedDraft);

        // Change the reivew status
        Long assignmentIdLong = Long.valueOf(assignmentId);
        reviewStatusSvc.notReviewedToInReview(assignmentIdLong, gerritChangeId);

        return ResponseEntity.ok(savedDraft);
    }

    /** Post a author's draft comment to gerrit and record in database */
    @PostMapping("/post-author-gerrit-draft-comment")
    public ResponseEntity<CommentInfoDto> postAuthorGerritDraftComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestParam("assignmentId") String assignmentId,
            @RequestBody CommentInputDto commentInput, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException, GitLabApiException {

        System.out.println("STAGE: ReviewController.postGerritDraftComment");
        ;
        String username = oauth2User.getAttribute("username").toString();

        CommentInfoDto savedDraft = gerritSvc.postGerritDraft(gerritChangeId, commentInput, username);

        // Save commentEntity to database
        commentSvc.recordAuthorDraftComment(gerritChangeId, assignmentId, savedDraft);

        // Change the reivew status
        Long assignmentIdLong = Long.valueOf(assignmentId);
        reviewStatusSvc.notReviewedToInReview(assignmentIdLong, gerritChangeId);

        return ResponseEntity.ok(savedDraft);
    }

    @PutMapping("/update-gerrit-draft-comment")
    public ResponseEntity<CommentInfoDto> updateGerritDraftComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestBody CommentInputDto commentInput, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException {

        System.out.println("STAGE: ReviewController.updateGerritDraftComment");

        String username = oauth2User.getAttribute("username").toString();

        return ResponseEntity.ok(gerritSvc.updateGerritDraft(
                gerritChangeId, commentInput, username));
    }

    @DeleteMapping("/delete-gerrit-draft-comment")
    public ResponseEntity<Void> deleteGerritDraftComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestParam("assignmentId") String assignmentId,
            @RequestBody CommentInputDto commentInput, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException, GitLabApiException {

        System.out.println("STAGE: ReviewController.deleteGerritDraftComment");

        gerritSvc.deleteGerritDraft(gerritChangeId, commentInput);

        // Delete the comment from the database
        commentSvc.deleteDraftComment(gerritChangeId, commentInput.getId());

        ;
        String username = oauth2User.getAttribute("username").toString();

        // If no more draft comments exist for this change, may change to NOT_REVIEWED
        List<CommentInfoDto> remainingDrafts = gerritSvc.getGerritChangeDraftComments(gerritChangeId, username);
        if (remainingDrafts.isEmpty()) {
            // Change the review status to NOT_REVIEWED
            Long assignmentIdLong = Long.valueOf(assignmentId);
            reviewStatusSvc.inReviewToNotReviewed(assignmentIdLong, gerritChangeId);
        }

        return ResponseEntity.noContent().build();
    }

    /*
     * Publish reviewer draft comments, mark them as published in the database,
     * and update the review status to NEED_RESOLVE or APPROVED.
     */
    @Transactional
    @PostMapping("/publish-reviewer-gerrit-draft-comments")
    public ResponseEntity<Void> publishReviewerDraftComments(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("needResolve") boolean needResolve,
            @RequestBody List<CommentInputDto> drafts) throws Exception {

        System.out.println("STAGE: ReviewController.publishReviewerDraftComments");

        // Convert CommentInputDto to List<String> draftIds
        List<String> draftIds = drafts.stream()
                .map(CommentInputDto::getId)
                .collect(Collectors.toList());

        gerritSvc.publishDrafts(gerritChangeId, draftIds);

        // Mark the comments as published in the database
        commentSvc.markCommentsPublished(gerritChangeId, draftIds);

        // Make review status as NEED_RESOLVE or APPROVED
        Long assignmentIdLong = Long.valueOf(assignmentId);
        if (needResolve) {
            reviewStatusSvc.inReviewToWaitingResolve(assignmentIdLong, gerritChangeId);

            List<String> repliedToCommentIds = drafts.stream()
                    .map(CommentInputDto::getInReplyTo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // If reply to author, send reply notification
            if (notificationSvc.isReplyToAuthor(gerritChangeId, repliedToCommentIds, assignmentIdLong)) {
                notificationSvc.sendNewReplyNotificationToAuthor(gerritChangeId, assignmentIdLong);
            } else {
                // If not reply to author, send change request notification
                notificationSvc.sendChangeRequestNotification(gerritChangeId, assignmentIdLong);
            }
        } else {
            reviewStatusSvc.inReviewOrResolveToApproved(assignmentIdLong, gerritChangeId);
            notificationSvc.sendApprovedNotification(gerritChangeId, assignmentIdLong);
        }

        return ResponseEntity.noContent().build();
    }

    /*
     * Publish author draft comments, mark them as published in the database,
     * not affecting the review status.
     */
    @Transactional
    @PostMapping("/publish-author-gerrit-draft-comments")
    public ResponseEntity<Void> publishAuthorDraftComments(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestParam("assignmentId") String assignmentId,
            @RequestBody List<CommentInputDto> drafts) throws Exception {

        System.out.println("STAGE: ReviewController.publishAuthorDraftComments");

        // Convert CommentInputDto to List<String> draftIds
        List<String> draftIds = drafts.stream()
                .map(CommentInputDto::getId)
                .collect(Collectors.toList());

        gerritSvc.publishDrafts(gerritChangeId, draftIds);

        // Mark the comments as published in the database
        commentSvc.markCommentsPublished(gerritChangeId, draftIds);

        // Nofity reviewers got replied...
        // Find replied reviewers
        Long assignmentIdLong = Long.valueOf(assignmentId);
        List<String> repliedToCommentIds = drafts.stream()
                .map(CommentInputDto::getInReplyTo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        notificationSvc.sendNewReplyNotificationToReviewer(gerritChangeId, repliedToCommentIds, assignmentIdLong);

        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PostMapping("/post-thumb-state-for-comment")
    public ResponseEntity<Void> postThumbStateForComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestParam("gerritCommentId") String gerritCommentId,
            @RequestParam("thumbState") ReactState thumbState) throws Exception {

        System.out.println("STAGE: ReviewController.postThumbStateForComment");

        commentSvc.markCommentReaction(gerritChangeId, gerritCommentId, thumbState);

        return ResponseEntity.noContent().build();
    }

}
