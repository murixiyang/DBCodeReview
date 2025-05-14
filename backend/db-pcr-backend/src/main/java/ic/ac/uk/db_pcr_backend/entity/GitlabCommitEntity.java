package ic.ac.uk.db_pcr_backend.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.ForeignKey;

@Entity
@Table(name = "gitlab_commits", uniqueConstraints = @UniqueConstraint(columnNames = "gitlab_commit_id"))
public class GitlabCommitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** GitLab SHA or internal commit ID */
    @Column(name = "gitlab_commit_id", nullable = false, unique = true, length = 64)
    private String gitlabCommitId;

    /** Which ProjectEntity (fork) this commit belongs to */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_commit_project"))
    private ProjectEntity ProjectEntity;

    /** Who authored the commit */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_commit_author"))
    private UserEntity author;

    /** Commit message/body */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /** When the commit was created in Git */
    @Column(name = "committed_at", nullable = false)
    private Instant committedAt;

    /** When we fetched it into our system */
    @CreationTimestamp
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private Instant fetchedAt;

    // --- Constructors ---
    public GitlabCommitEntity() {
    }

    public GitlabCommitEntity(String gitlabCommitId,
            ProjectEntity ProjectEntity,
            UserEntity author,
            String message,
            Instant committedAt) {
        this.gitlabCommitId = gitlabCommitId;
        this.ProjectEntity = ProjectEntity;
        this.author = author;
        this.message = message;
        this.committedAt = committedAt;
    }

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public String getGitlabCommitId() {
        return gitlabCommitId;
    }

    public void setGitlabCommitId(String gitlabCommitId) {
        this.gitlabCommitId = gitlabCommitId;
    }

    public ProjectEntity getProject() {
        return ProjectEntity;
    }

    public void setProject(ProjectEntity ProjectEntity) {
        this.ProjectEntity = ProjectEntity;
    }

    public UserEntity getAuthor() {
        return author;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCommittedAt() {
        return committedAt;
    }

    public void setCommittedAt(Instant committedAt) {
        this.committedAt = committedAt;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }
}