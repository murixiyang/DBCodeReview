package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.model.ReviewStatus;

public class ChangeRequestDto {
    private Long id;
    private Long assignmentId;
    private Long commitId;
    private String gerritChangeId;
    private String message;
    private Instant submittedAt;
    private ReviewStatus status;
    private Instant lastStatusAt;

    public ChangeRequestDto() {
    }

    public ChangeRequestDto(Long id, Long assignmentId, Long commitId, String gerritChangeId, String message,
            Instant submittedAt, ReviewStatus status, Instant lastStatusAt) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.commitId = commitId;
        this.gerritChangeId = gerritChangeId;
        this.message = message;
        this.submittedAt = submittedAt;
        this.status = status;
        this.lastStatusAt = lastStatusAt;
    }

    public static ChangeRequestDto fromEntity(ChangeRequestEntity changeRequest) {
        return new ChangeRequestDto(
                changeRequest.getId(),
                changeRequest.getAssignment().getId(),
                changeRequest.getCommit().getId(),
                changeRequest.getGerritChangeId(),
                changeRequest.getCommit().getMessage(),
                changeRequest.getSubmittedAt(),
                changeRequest.getStatus(),
                changeRequest.getLastStatusAt());
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getCommitId() {
        return commitId;
    }

    public void setCommitId(Long commitId) {
        this.commitId = commitId;
    }

    public String getGerritChangeId() {
        return gerritChangeId;
    }

    public void setGerritChangeId(String gerritChangeId) {
        this.gerritChangeId = gerritChangeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public Instant getLastStatusAt() {
        return lastStatusAt;
    }

    public void setLastStatusAt(Instant lastStatusAt) {
        this.lastStatusAt = lastStatusAt;
    }
}