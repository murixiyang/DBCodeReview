package ic.ac.uk.db_pcr_backend.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "gitlab_groups", uniqueConstraints = @UniqueConstraint(columnNames = "gitlab_group_id"))
public class GitlabGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gitlab_group_id", nullable = false, unique = true)
    private Long gitlabGroupId;

    @Column(nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructor
    public GitlabGroupEntity() {
    }

    public GitlabGroupEntity(Long gitlabGroupId, String name) {
        this.gitlabGroupId = gitlabGroupId;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Long getGitlabGroupId() {
        return gitlabGroupId;
    }

    public void setGitlabGroupId(Long gitlabGroupId) {
        this.gitlabGroupId = gitlabGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
