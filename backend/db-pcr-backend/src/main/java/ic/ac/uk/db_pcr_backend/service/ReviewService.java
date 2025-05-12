package ic.ac.uk.db_pcr_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.dto.AssignmentMetadataDto;
import ic.ac.uk.db_pcr_backend.entity.PseudoNameEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.repository.PseudoNameRepository;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final GitLabService gitLabSvc;

    private final ReviewAssignmentRepository reviewAssignmentRepo;
    private final PseudoNameRepository nameRepo;

    public ReviewService(GitLabService gitLabSvc,
            ReviewAssignmentRepository reviewAssignmentRepo,
            PseudoNameRepository nameRepo) {
        this.gitLabSvc = gitLabSvc;
        this.reviewAssignmentRepo = reviewAssignmentRepo;
        this.nameRepo = nameRepo;
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
    @Transactional(readOnly = true)
    public List<AssignmentMetadataDto> findAssignmentsForReviewer(String reviewerName) {

        return reviewAssignmentRepo.findByReviewerName(reviewerName).stream()
                .map(asn -> {
                    String authorPseudo = nameRepo
                            .findByAssignmentUuidAndRealName(asn.getAssignmentUuid(), asn.getAuthorName())
                            .map(PseudoNameEntity::getPseudoName)
                            .orElseThrow();
                    String reviewerPseudo = nameRepo
                            .findByAssignmentUuidAndRealName(asn.getAssignmentUuid(), asn.getReviewerName())
                            .map(PseudoNameEntity::getPseudoName)
                            .orElseThrow();
                    return new AssignmentMetadataDto(
                            asn.getAssignmentUuid(),
                            asn.getProjectName(),
                            authorPseudo,
                            reviewerPseudo);
                })
                .toList();
    }

}
