package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.ChangeStatus;

public class ReviewAssignmentDto {
    private Long id;
    private Long authorId;
    private Long reviewerId;
    private Long projectId;
    private Instant assignedAt;
    private ChangeStatus projectStatus;
    private Instant projectStatusAt;

    // Constructor
    public ReviewAssignmentDto() {
    }

    public ReviewAssignmentDto(Long id, Long authorId, Long reviewerId, Long projectId, Instant assignedAt,
            ChangeStatus projectStatus, Instant projectStatusAt) {
        this.id = id;
        this.authorId = authorId;
        this.reviewerId = reviewerId;
        this.projectId = projectId;
        this.assignedAt = assignedAt;
        this.projectStatus = projectStatus;
        this.projectStatusAt = projectStatusAt;
    }

    public static ReviewAssignmentDto fromEntity(ReviewAssignmentEntity ra) {
        return new ReviewAssignmentDto(
                ra.getId(),
                ra.getAuthor().getId(),
                ra.getReviewer().getId(),
                ra.getProject().getId(),
                ra.getAssignedAt(),
                ra.getProjectStatus(),
                ra.getProjectStatusAt());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public ChangeStatus getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(ChangeStatus projectStatus) {
        this.projectStatus = projectStatus;
    }

    public Instant getProjectStatusAt() {
        return projectStatusAt;
    }

    public void setProjectStatusAt(Instant projectStatusAt) {
        this.projectStatusAt = projectStatusAt;
    }

}
