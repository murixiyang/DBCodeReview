package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.ProjectUserPseudonymEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.model.ProjectStatus;

public class ReviewAssignmentPseudonymDto {
    private Long id;
    private String authorPseudonym;
    private String reviewerPseudonym;
    private Long groupProjectId;
    private String groupProjectName;
    private Instant assignedAt;
    private ProjectStatus projectStatus;
    private Instant projectStatusAt;

    // Constructor
    public ReviewAssignmentPseudonymDto() {
    }

    public ReviewAssignmentPseudonymDto(
            ReviewAssignmentEntity ra,
            ProjectUserPseudonymEntity authorMask,
            ProjectUserPseudonymEntity reviewerMask) {
        this.id = ra.getId();
        this.authorPseudonym = authorMask.getPseudonym().getName();
        this.reviewerPseudonym = reviewerMask.getPseudonym().getName();
        this.groupProjectId = ra.getGroupProject().getId();
        this.groupProjectName = ra.getGroupProject().getName();
        this.assignedAt = ra.getAssignedAt();
        this.projectStatus = ra.getProjectStatus();
        this.projectStatusAt = ra.getProjectStatusAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthorPseudonym() {
        return authorPseudonym;
    }

    public void setAuthorPseudonym(String authorPseudonym) {
        this.authorPseudonym = authorPseudonym;
    }

    public String getReviewerPseudonym() {
        return reviewerPseudonym;
    }

    public void setReviewerPseudonym(String reviewerPseudonym) {
        this.reviewerPseudonym = reviewerPseudonym;
    }

    public Long getGroupProjectId() {
        return groupProjectId;
    }

    public void setGroupProjectId(Long groupProjectId) {
        this.groupProjectId = groupProjectId;
    }

    public String getGroupProjectName() {
        return groupProjectName;
    }

    public void setGroupProjectName(String groupProjectName) {
        this.groupProjectName = groupProjectName;
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
