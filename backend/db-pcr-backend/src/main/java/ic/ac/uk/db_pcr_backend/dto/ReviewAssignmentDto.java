package ic.ac.uk.db_pcr_backend.dto;

public class ReviewAssignmentDto {

    private String assignmentUuid;
    private String groupProjectId;
    private String forkProjectId;
    private String reviewerName;

    /**
     * Never exposes the authorName in URLs—only assignmentUuid and both IDs.
     */

    // Constructors
    public ReviewAssignmentDto(
            String assignmentUuid,
            String groupProjectId,
            String forkProjectId,
            String reviewerName) {
        this.assignmentUuid = assignmentUuid;
        this.groupProjectId = groupProjectId;
        this.forkProjectId = forkProjectId;
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

    public String getForkProjectId() {
        return forkProjectId;
    }

    public void setForkProjectId(String forkProjectId) {
        this.forkProjectId = forkProjectId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

}
