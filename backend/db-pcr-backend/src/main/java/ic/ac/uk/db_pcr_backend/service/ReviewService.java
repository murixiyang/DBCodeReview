package ic.ac.uk.db_pcr_backend.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.dto.gerritdto.ChangeDiffDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.ChangeInfoDto;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.repository.PseudonymRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.CommitInfo;
import com.google.gerrit.extensions.restapi.RestApiException;

@Service
public class ReviewService {

    @Autowired
    private GitLabService gitLabSvc;

    @Autowired
    private GerritService gerritSvc;

    @Autowired
    private PseudoNameService pseudoNameSvc;

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Autowired
    private PseudonymRepo nameRepo;

    /** Get Assignment Metadata for the reviewer. Assign pseudoname if not yet */
    @Transactional
    public List<AssignmentMetadataDto> findAssignmentsForReviewer(String reviewerName) {

        // Get all assignments for the reviewer
        List<ReviewAssignmentEntity> assignments = reviewAssignmentRepo
                .findByReviewerName(reviewerName);

        // For each assignment, create pseudonames
        List<AssignmentMetadataDto> assignmentDtos = new ArrayList<>();
        for (ReviewAssignmentEntity asn : assignments) {
            String authorPseudo = pseudoNameSvc.getOrCreatePseudoName(asn.getAssignmentUuid(), asn.getAuthorName());
            String reviewerPseudo = pseudoNameSvc.getOrCreatePseudoName(asn.getAssignmentUuid(), asn.getReviewerName());

            AssignmentMetadataDto dto = new AssignmentMetadataDto(
                    asn.getAssignmentUuid(),
                    asn.getProjectName(),
                    authorPseudo,
                    reviewerPseudo);
            assignmentDtos.add(dto);
        }

        return assignmentDtos;
    }

    /** Fetch commits list using Uuid */
    public List<ChangeInfoDto> fetchCommitsForAssignment(String assignmentUuid) throws Exception {
        ReviewAssignmentEntity asn = reviewAssignmentRepo.findByAssignmentUuid(assignmentUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // build the path
        String repoPath = asn.getAuthorName() + "/" + asn.getProjectName();

        // call Gerrit
        List<ChangeInfo> changes = gerritSvc.getCommitsFromProjectPath(repoPath);

        return changes.stream()
                .map(change -> {

                    return new ChangeInfoDto(
                            change.changeId,
                            change.subject,
                            change.updated.toInstant());
                })
                .collect(Collectors.toList());
    }

    // * Get ChangeDiff by changeId */
    public String getDiffs(String changeId) throws RestApiException {
        return gerritSvc.fetchRawPatch(changeId, "current");
    }

}
