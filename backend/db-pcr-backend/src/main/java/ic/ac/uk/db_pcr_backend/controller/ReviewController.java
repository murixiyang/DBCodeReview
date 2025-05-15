package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

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

import ic.ac.uk.db_pcr_backend.dto.datadto.ChangeRequestDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.ProjectDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.ReviewAssignmentPseudonymDto;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.RoleType;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;
import ic.ac.uk.db_pcr_backend.service.PseudoNameService;
import ic.ac.uk.db_pcr_backend.service.ReviewService;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

        @Autowired
        private ReviewService reviewSvc;

        @Autowired
        private PseudoNameService pseudoNameSvc;

        @Autowired
        private UserRepo userRepo;

        @Autowired
        private ProjectRepo projectRepo;

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

        /**
         * Return commit list for a project author submitted to review
         */
        @Transactional(readOnly = true)
        @GetMapping("/get-review-project-commits")
        public ResponseEntity<List<ChangeRequestDto>> getReviewProjectCommits(
                        @RequestParam("projectId") String projectId,
                        @RequestParam("username") String username,
                        @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

                // Find the project
                ProjectEntity project = projectRepo.findByGitlabProjectId(Long.valueOf(projectId))
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Project not found: " + projectId));

                // Find the review assignment
                List<ReviewAssignmentEntity> assignments = reviewAssignmentRepo.findByProject(project);

                System.out.println("DBLOG: Assignment: " + assignments);

                // Find the change requests
                List<ChangeRequestDto> changeRequests = assignments.stream()
                                .flatMap(asn -> changeRequestRepo.findByAssignment(asn).stream())
                                .map(ChangeRequestDto::fromEntity)
                                .collect(Collectors.toList());

                System.out.println("DBLOG: Change Requests: " + changeRequests);

                return ResponseEntity.ok(changeRequests);
        }

        @Transactional(readOnly = true)
        @GetMapping("/get-review-assignment-pseudonym-by-id")
        public ResponseEntity<ReviewAssignmentPseudonymDto> getReviewAssignmentById(
                        @RequestParam("assignmentId") Long assignmentId) throws Exception {

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

        /** Get assignment metadata for reviewer */
        // @GetMapping("/get-metadata-by-reviewer")
        // public List<AssignmentMetadataDto>
        // getMyAssignmentsForReviewer(@RequestParam("reviewerName") String
        // reviewerName,
        // @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
        // throws Exception {
        // return reviewSvc.findAssignmentsForReviewer(reviewerName);
        // }

        /** Get assignment metadata for uuid */
        // @GetMapping("/get-metadata-by-uuid")
        // public List<AssignmentMetadataDto>
        // getMyAssignmentsByUuid(@RequestParam("assignmentUuid") String assignmentUuid)
        // throws Exception {
        // return reviewSvc.findAssignmentsForReviewer(assignmentUuid);
        // }

        /** Get Gerrit ChangeDiff via Uuid and ChangeId */
        @GetMapping("/get-change-diff")
        public String getChangeDiff(@RequestParam("assignmentUuid") String assignmentUuid,
                        @RequestParam("changeId") String changeId) throws Exception {
                return reviewSvc.getDiffs(changeId);
        }

}
