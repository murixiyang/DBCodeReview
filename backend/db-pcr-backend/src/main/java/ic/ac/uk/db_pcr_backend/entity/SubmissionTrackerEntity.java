package ic.ac.uk.db_pcr_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "submission_tracker", uniqueConstraints = @UniqueConstraint(columnNames = { "username", "projectId" }))
public class SubmissionTrackerEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String projectId;
    private String lastSubmittedSha;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getLastSubmittedSha() {
        return lastSubmittedSha;
    }

    public void setLastSubmittedSha(String lastSubmittedSha) {
        this.lastSubmittedSha = lastSubmittedSha;
    }
}
