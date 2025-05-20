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
@Table(name = "submission_tracker", uniqueConstraints = @UniqueConstraint(columnNames = { "author_id", "project_id",
        "commit_id" }, name = "uk_submission_event"))

public class SubmissionTrackerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Who is submitting (the author) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_st_author"))
    private UserEntity author;

    /** Which project they’re submitting *from* */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_st_project"))
    private ProjectEntity project;

    /** The last SHA we pushed to Gerrit */
    @Column(name = "last_submitted_gerrit_sha", nullable = false)
    private String lastSubmittedGerritSha;

    /** The last Gitlab Commit we pushed to GitLab */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "commit_id", foreignKey = @ForeignKey(name = "fk_se_commit"))
    private GitlabCommitEntity lastSubmittedCommit;

    /** When this row was submitted */
    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    public SubmissionTrackerEntity() {
    }

    public SubmissionTrackerEntity(UserEntity author, ProjectEntity project, String lastSubmittedGerritSha,
            GitlabCommitEntity lastSubmittedCommit) {
        this.author = author;
        this.project = project;
        this.lastSubmittedGerritSha = lastSubmittedGerritSha;
        this.lastSubmittedCommit = lastSubmittedCommit;
    }

    // --- getters & setters ---
    public Long getId() {
        return id;
    }

    public UserEntity getAuthor() {
        return author;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public String getLastSubmittedGerritSha() {
        return lastSubmittedGerritSha;
    }

    public void setLastSubmittedGerritSha(String lastSubmittedGerritSha) {
        this.lastSubmittedGerritSha = lastSubmittedGerritSha;
    }

    public GitlabCommitEntity getLastSubmittedCommit() {
        return lastSubmittedCommit;
    }

    public void setLastSubmittedCommit(GitlabCommitEntity lastSubmittedCommit) {
        this.lastSubmittedCommit = lastSubmittedCommit;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}
