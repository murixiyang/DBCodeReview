package ic.ac.uk.db_pcr_backend.dto;
import ic.ac.uk.db_pcr_backend.entity.ReviewStatusEntity.ReviewStatus;

public class ReviewStatusDto {

    private String username;
    private String projectId;
    private String commitSha;
    private ReviewStatus status;

    // Constructors
    public ReviewStatusDto() {
    }

    public ReviewStatusDto(String username, String projectId, String commitSha, ReviewStatus status) {
        this.username = username;
        this.projectId = projectId;
        this.commitSha = commitSha;
        this.status = status;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }
}
