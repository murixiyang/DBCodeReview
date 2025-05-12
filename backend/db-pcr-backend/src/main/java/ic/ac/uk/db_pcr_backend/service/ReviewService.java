package ic.ac.uk.db_pcr_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.dto.AssignmentMetadataDto;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepository;
import jakarta.transaction.Transactional;

@Service
public class ReviewService {

    private final GitLabService gitLabSvc;
    private final PseudoNameService pseudonameSvc;
    private final ReviewAssignmentRepository reviewAssignmentRepo;

    public ReviewService(GitLabService gitLabSvc,
            PseudoNameService pseudonameSvc,
            ReviewAssignmentRepository reviewAssignmentRepo) {
        this.gitLabSvc = gitLabSvc;
        this.pseudonameSvc = pseudonameSvc;
        this.reviewAssignmentRepo = reviewAssignmentRepo;
    }

    /** Get projects that a user is assigned as reviewer */
    public List<Project> getProjectsToReview(String username, String groupId, String oauthToken)
            throws GitLabApiException {
        // Get project where the user is reviewer
        List<ReviewAssignmentEntity> assigns = reviewAssignmentRepo.findByReviewerName(username);

        // Get group project Id
        Set<String> groupProjectIds = assigns.stream()
                .map(ReviewAssignmentEntity::getGroupProjectId)
                .collect(Collectors.toSet());

        // Fetch project from GitLab
        List<Project> projects = new ArrayList<>();
        for (String projectId : groupProjectIds) {
            Project project = gitLabSvc.getGroupProjectById(groupId, projectId, oauthToken);
            projects.add(project);
        }

        return projects;
    }

    /** Get Assignment Metadata for the reviewer. Assign pseudoname if not yet */
    @Transactional
    public List<AssignmentMetadataDto> findAssignmentsForReviewer(String reviewerName) {

        // Get all assignments for the reviewer
        List<ReviewAssignmentEntity> assignments = reviewAssignmentRepo
                .findByReviewerName(reviewerName);

        // For each assignment, create pseudonames
        List<AssignmentMetadataDto> assignmentDtos = new ArrayList<>();
        for (ReviewAssignmentEntity asn : assignments) {
            String authorPseudo = pseudonameSvc.getOrCreatePseudoName(asn.getAssignmentUuid(), asn.getAuthorName());
            String reviewerPseudo = pseudonameSvc.getOrCreatePseudoName(asn.getAssignmentUuid(), asn.getReviewerName());

            AssignmentMetadataDto dto = new AssignmentMetadataDto(
                    asn.getAssignmentUuid(),
                    asn.getProjectName(),
                    authorPseudo,
                    reviewerPseudo);
            assignmentDtos.add(dto);
        }

        return assignmentDtos;
    }

}
