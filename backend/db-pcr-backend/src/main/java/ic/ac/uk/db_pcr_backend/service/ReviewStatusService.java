package ic.ac.uk.db_pcr_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.model.ReviewStatus;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;

@Service
public class ReviewStatusService {

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    // * Change review status from NOT_REVIEWED to IN_REVIEW when posting draft
    // comments */
    public void notReviewedToInReview(Long assignmentId, String changeId) {
        System.out.println("Service: ReviewStatusService.changeReviewStatusToInReview");

        // Find assignment by ID
        var assignment = reviewAssignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found by id: " + assignmentId));

        // Find chagne request by assignment and change ID
        var changeRequest = changeRequestRepo.findByAssignmentAndGerritChangeId(assignment, changeId)
                .orElseThrow(() -> new IllegalArgumentException("Change request not found for assignment ID: "
                        + assignmentId + " and change ID: " + changeId));

        // Check the change request status, if NOT_REVIEWED, then change to IN_REVIEW
        if (changeRequest.getStatus() == ReviewStatus.NOT_REVIEWED) {
            changeRequest.setStatus(ReviewStatus.IN_REVIEW);
        }

        // Otherwise preserve the current status (e.g. IN_REVIEW, NEED_RESOLVE)
    }

    // * Change review status from IN_REVIEW to NOT_REVIEWED when removing draft
    // comments */
    public void inReviewToNotReviewed(Long assignmentId, String changeId) {
        System.out.println("Service: ReviewStatusService.inReviewToNotReviewed");

        // Find assignment by ID
        var assignment = reviewAssignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found by id: " + assignmentId));

        // Find chagne request by assignment and change ID
        var changeRequest = changeRequestRepo.findByAssignmentAndGerritChangeId(assignment, changeId)
                .orElseThrow(() -> new IllegalArgumentException("Change request not found for assignment ID: "
                        + assignmentId + " and change ID: " + changeId));

        // Check the change request status, if NOT_REVIEWED, then change to IN_REVIEW
        if (changeRequest.getStatus() == ReviewStatus.IN_REVIEW) {
            changeRequest.setStatus(ReviewStatus.NOT_REVIEWED);
        }

        // Otherwise preserve the current status (e.g. IN_REVIEW, NEED_RESOLVE)
    }

    // * Change review status from IN_REVIEW to WAITING_RESOLVE when publishing
    // drafts
    // */
    public void inReviewToWaitingResolve(Long assignmentId, String changeId) {
        System.out.println("Service: ReviewStatusService.inReviewToWaitingResolve");

        // Find assignment by ID
        var assignment = reviewAssignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found by id: " + assignmentId));

        // Find chagne request by assignment and change ID
        var changeRequest = changeRequestRepo.findByAssignmentAndGerritChangeId(assignment, changeId)
                .orElseThrow(() -> new IllegalArgumentException("Change request not found for assignment ID: "
                        + assignmentId + " and change ID: " + changeId));

        // Check the change request status, if NOT_REVIEWED, then change to IN_REVIEW
        if (changeRequest.getStatus() == ReviewStatus.IN_REVIEW) {
            changeRequest.setStatus(ReviewStatus.WAITING_RESOLVE);
        }

        // Otherwise preserve the current status (e.g. IN_REVIEW, NEED_RESOLVE)
    }

    // * Change review status from IN_REVIEW or WAITING_RESOLVE to APPROVED when
    // publishing drafts */
    public void inReviewOrResolveToApproved(Long assignmentId, String changeId) {
        System.out.println("Service: ReviewStatusService.inReviewToApproved");

        // Find assignment by ID
        var assignment = reviewAssignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found by id: " + assignmentId));

        // Find chagne request by assignment and change ID
        var changeRequest = changeRequestRepo.findByAssignmentAndGerritChangeId(assignment, changeId)
                .orElseThrow(() -> new IllegalArgumentException("Change request not found for assignment ID: "
                        + assignmentId + " and change ID: " + changeId));

        // Check the change request status, if NOT_REVIEWED, then change to IN_REVIEW
        if (changeRequest.getStatus() == ReviewStatus.IN_REVIEW
                || changeRequest.getStatus() == ReviewStatus.WAITING_RESOLVE) {
            changeRequest.setStatus(ReviewStatus.APPROVED);
        }

        // Otherwise preserve the current status (e.g. IN_REVIEW, NEED_RESOLVE)
    }

}
