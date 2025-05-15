package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.ChangeStatus;

public class ReviewAssignmentUsernameDto {
    private Long id;
    private String authorName;
    private String reviewerName;
    private Long projectId;
    private Instant assignedAt;
    private ChangeStatus projectStatus;
    private Instant projectStatusAt;

    // Constructor
    public ReviewAssignmentUsernameDto() {
    }

    public ReviewAssignmentUsernameDto(Long id, String authorName, String reviewerName, Long projectId,
            Instant assignedAt,
            ChangeStatus projectStatus, Instant projectStatusAt) {
        this.id = id;
        this.authorName = authorName;
        this.reviewerName = reviewerName;
        this.projectId = projectId;
        this.assignedAt = assignedAt;
        this.projectStatus = projectStatus;
        this.projectStatusAt = projectStatusAt;
    }

    public ReviewAssignmentUsernameDto(
            ReviewAssignmentEntity ra,
            UserEntity author,
            UserEntity reviewer) {
        this.id = ra.getId();
        this.authorName = author.getUsername();
        this.reviewerName = reviewer.getUsername();
        this.projectStatus = ra.getProjectStatus();
        this.assignedAt = ra.getAssignedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
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
