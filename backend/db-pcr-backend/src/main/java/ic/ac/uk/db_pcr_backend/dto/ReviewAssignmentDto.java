package ic.ac.uk.db_pcr_backend.dto;

public class ReviewAssignmentDto {

    private String projectId;
    private String projectName;
    private String authorName;
    private String reviewerName;

    // Constructors
    public ReviewAssignmentDto(String projectId, String projectName, String authorName, String reviewerName) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.authorName = authorName;
        this.reviewerName = reviewerName;
    }

    // Getters and Setters

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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
