package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.ProjectUserPseudonymEntity;
import ic.ac.uk.db_pcr_backend.entity.PseudonymEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.model.ChangeStatus;

public class ReviewAssignmentPseudonymDto {
    private Long id;
    private String authorPseudonym;
    private String reviewerPseudonym;
    private Long projectId;
    private Instant assignedAt;
    private ChangeStatus projectStatus;
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
