package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.dto.datadto.ChangeRequestDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.ProjectDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.ReviewAssignmentPseudonymDto;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.RoleType;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabCommitRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;
import ic.ac.uk.db_pcr_backend.service.GerritService;
import ic.ac.uk.db_pcr_backend.service.PseudoNameService;
import ic.ac.uk.db_pcr_backend.service.ReviewService;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private GerritService gerritSvc;

    @Autowired
    private ReviewService reviewSvc;

    @Autowired
    private PseudoNameService pseudoNameSvc;

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
            @RequestParam("projectId") String projectId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

        System.out.println("STAGE: ReviewController.getReviewProjectCommits");

        // Find the project
        ProjectEntity project = projectRepo.findById(Long.valueOf(projectId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + projectId));

        // Find the review assignment
        List<ReviewAssignmentEntity> assignments = reviewAssignmentRepo.findByGroupProject(project.getParentProject());

        // Find change requests, if not found, then create them

        // Find the change requests
        List<ChangeRequestDto> changeRequests = assignments.stream()
                .flatMap(asn -> changeRequestRepo.findByAssignment(asn).stream())
                .map(ChangeRequestDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(changeRequests);
    }

    /**
     * Get the review assignment pseudonym by id
     */
    @Transactional(readOnly = true)
    @GetMapping("/get-review-assignment-pseudonym-by-id")
    public ResponseEntity<ReviewAssignmentPseudonymDto> getReviewAssignmentById(
            @RequestParam("assignmentId") Long assignmentId) throws Exception {

        System.out.println("STAGE: ReviewController.getReviewAssignmentById");

        ReviewAssignmentEntity assignment = reviewAssignmentRepo.findById(assignmentId).orElseThrow(
                () -> new IllegalArgumentException("Unknown ReviewAssignment id " + assignmentId));

        // Get the author and reviewer pseudonyms
        var authorMask = pseudoNameSvc.getPseudonymInReviewAssignment(assignment, RoleType.AUTHOR);
        var reviewerMask = pseudoNameSvc.getPseudonymInReviewAssignment(assignment, RoleType.REVIEWER);

        // Create the DTO
        ReviewAssignmentPseudonymDto dto = new ReviewAssignmentPseudonymDto(assignment, authorMask,
                reviewerMask);

        return ResponseEntity.ok(dto);
    }

    /** Get Gerrit ChangeDiff via Uuid and ChangeId */
    @GetMapping("/get-change-diff")
    public String getChangeDiff(@RequestParam("assignmentUuid") String assignmentUuid,
            @RequestParam("changeId") String changeId) throws Exception {

        System.out.println("STAGE: ReviewController.getChangeDiff");

        return reviewSvc.getDiffs(changeId);
    }

    /** Push some gitlab commits to gerrit */
    @PostMapping("/post-request-review")
    public ResponseEntity<Map<String, String>> requestReview(
            @RequestParam("projectId") String projectId,
            @RequestParam("sha") String gitlabCommitId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
            throws Exception {

        System.out.println("STAGE: GerritController.requestReview");

        System.out.println("DBLOG: sha: " + gitlabCommitId);

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

}
