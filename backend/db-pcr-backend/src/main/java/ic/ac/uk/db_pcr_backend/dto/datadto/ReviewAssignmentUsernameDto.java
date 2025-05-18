package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.ProjectStatus;

public class ReviewAssignmentUsernameDto {
    private Long id;
    private String authorName;
    private String reviewerName;
    private Long groupProjectId;
    private Instant assignedAt;
    private ProjectStatus projectStatus;
    private Instant projectStatusAt;

    // Constructor
    public ReviewAssignmentUsernameDto() {
    }

    public ReviewAssignmentUsernameDto(Long id, String authorName, String reviewerName, Long groupProjectId,
            Instant assignedAt,
            ProjectStatus projectStatus, Instant projectStatusAt) {
        this.id = id;
        this.authorName = authorName;
        this.reviewerName = reviewerName;
        this.groupProjectId = groupProjectId;
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

    public Long getGroupProjectId() {
        return groupProjectId;
    }

    public void setGroupProjectId(Long groupProjectId) {
        this.groupProjectId = groupProjectId;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public ProjectStatus getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(ProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
    }

    public Instant getProjectStatusAt() {
        return projectStatusAt;
    }

    public void setProjectStatusAt(Instant projectStatusAt) {
        this.projectStatusAt = projectStatusAt;
    }

}
