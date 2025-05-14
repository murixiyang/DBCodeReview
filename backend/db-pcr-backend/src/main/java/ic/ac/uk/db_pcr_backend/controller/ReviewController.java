package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.dto.AssignmentMetadataDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.ProjectDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.ChangeDiffDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.ChangeInfoDto;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;
import ic.ac.uk.db_pcr_backend.service.ReviewService;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewSvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Value("${gitlab.group.id}")
    private String groupId;

    /**
     * Return all the Projects this username is a reviewer *for*,
     * based on the assignments table.
     */
    @Transactional(readOnly = true)
    @GetMapping("/get-projects-to-review")
    public ResponseEntity<List<ProjectDto>> getProjectsToReview(
            @RequestParam("username") String username,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

        UserEntity reviewer = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + username));

        List<ReviewAssignmentEntity> assignments = reviewAssignmentRepo.findByReviewer(reviewer);

        List<ProjectDto> projects = assignments.stream()
                .map(ReviewAssignmentEntity::getProject)
                .distinct()
                .map(ProjectDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(projects);
    }

    /** Get assignment metadata for reviewer */
    // @GetMapping("/get-metadata-by-reviewer")
    // public List<AssignmentMetadataDto> getMyAssignmentsForReviewer(@RequestParam("reviewerName") String reviewerName,
    //         @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {
    //     return reviewSvc.findAssignmentsForReviewer(reviewerName);
    // }

    /** Get assignment metadata for uuid */
    // @GetMapping("/get-metadata-by-uuid")
    // public List<AssignmentMetadataDto> getMyAssignmentsByUuid(@RequestParam("assignmentUuid") String assignmentUuid)
    //         throws Exception {
    //     return reviewSvc.findAssignmentsForReviewer(assignmentUuid);
    // }

    /** Get Gerrit ChangeInfo List via Uuid */
    @GetMapping("/get-commit-list-by-uuid")
    public List<ChangeInfoDto> getCommitListByUuid(@RequestParam("assignmentUuid") String assignmentUuid)
            throws Exception {
        return reviewSvc.fetchCommitsForAssignment(assignmentUuid);
    }

    /** Get Gerrit ChangeDiff via Uuid and ChangeId */
    @GetMapping("/get-change-diff")
    public String getChangeDiff(@RequestParam("assignmentUuid") String assignmentUuid,
            @RequestParam("changeId") String changeId) throws Exception {
        return reviewSvc.getDiffs(changeId);
    }

}
