package ic.ac.uk.db_pcr_backend.dto;

public class ReviewAssignmentDto {

    private String assignmentUuid;
    private String groupProjectId;
    private String projectName;
    private String authorName;
    private String reviewerName;

    // Constructors
    public ReviewAssignmentDto(
            String assignmentUuid,
            String groupProjectId,
            String projectName,
            String authorName,
            String reviewerName) {
        this.assignmentUuid = assignmentUuid;
        this.groupProjectId = groupProjectId;
        this.projectName = projectName;
        this.authorName = authorName;
        this.reviewerName = reviewerName;
    }

    // Getters and Setters
    public String getAssignmentUuid() {
        return assignmentUuid;
    }

    public void setAssignmentUuid(String assignmentUuid) {
        this.assignmentUuid = assignmentUuid;
    }

    public String getGroupProjectId() {
        return groupProjectId;
    }

    public void setGroupProjectId(String groupProjectId) {
        this.groupProjectId = groupProjectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

}
