package ic.ac.uk.db_pcr_backend.entity;

import java.time.Instant;

import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "submission_tracker", uniqueConstraints = @UniqueConstraint(columnNames = { "author_id",
        "project_id" }, name = "uk_submission_author_project"))

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
    @Column(name = "last_submitted_sha", nullable = false)
    private String lastSubmittedSha;

    /** When this row was last updated */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SubmissionTrackerEntity() {
    }

    public SubmissionTrackerEntity(UserEntity author, ProjectEntity project, String lastSubmittedSha) {
        this.author = author;
        this.project = project;
        this.lastSubmittedSha = lastSubmittedSha;
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

    public String getLastSubmittedSha() {
        return lastSubmittedSha;
    }

    public void setLastSubmittedSha(String lastSubmittedSha) {
        this.lastSubmittedSha = lastSubmittedSha;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
