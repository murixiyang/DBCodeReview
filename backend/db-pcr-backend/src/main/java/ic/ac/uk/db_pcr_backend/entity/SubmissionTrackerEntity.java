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

    /** Link to the previous submission in this chain */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "previous_submission_id", foreignKey = @ForeignKey(name = "fk_st_prev_submission"))
    private SubmissionTrackerEntity previousSubmission;

    /** New SHA we pushed to Gerrit */
    @Column(name = "submitted_gerrit_sha", nullable = false)
    private String submittedGerritSha;

    /** The last Gitlab Commit we pushed to GitLab */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "commit_id", foreignKey = @ForeignKey(name = "fk_se_commit"))
    private GitlabCommitEntity submittedCommit;

    /** When this row was submitted */
    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    public SubmissionTrackerEntity() {
    }

    public SubmissionTrackerEntity(UserEntity author, ProjectEntity project, SubmissionTrackerEntity previousSubmission,
            String submittedGerritSha,
            GitlabCommitEntity submittedCommit) {
        this.author = author;
        this.project = project;
        this.submittedGerritSha = submittedGerritSha;
        this.previousSubmission = previousSubmission;
        this.submittedCommit = submittedCommit;
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

    public SubmissionTrackerEntity getPreviousSubmission() {
        return previousSubmission;
    }

    public void setPreviousSubmission(SubmissionTrackerEntity previousSubmission) {
        this.previousSubmission = previousSubmission;
    }

    public String getSubmittedGerritSha() {
        return submittedGerritSha;
    }

    public void setSubmittedGerritSha(String submittedGerritSha) {
        this.submittedGerritSha = submittedGerritSha;
    }

    public GitlabCommitEntity getSubmittedCommit() {
        return submittedCommit;
    }

    public void setSubmittedCommit(GitlabCommitEntity submittedCommit) {
        this.submittedCommit = submittedCommit;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}
