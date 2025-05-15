package ic.ac.uk.db_pcr_backend.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import ic.ac.uk.db_pcr_backend.model.ReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "change_requests", uniqueConstraints = @UniqueConstraint(columnNames = "gerrit_change_id"))
public class ChangeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Which assignment (author↔reviewer) this request belongs to */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cr_assignment"))
    private ReviewAssignmentEntity assignment;

    /** The commit the author submitted for review */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "commit_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cr_commit"))
    private GitlabCommitEntity commit;

    /** Gerrit Change-ID */
    @Column(name = "gerrit_change_id", nullable = false, unique = true)
    private String gerritChangeId;

    /** When the author submitted into Gerrit */
    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    /** Current status of this change in the review workflow */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.NOT_REVIEWED;

    /** When the status last changed */
    @UpdateTimestamp
    @Column(name = "last_status_at", nullable = false)
    private Instant lastStatusAt;

    // --- Constructors ---
    public ChangeRequestEntity() {
    }

    public ChangeRequestEntity(ReviewAssignmentEntity assignment,
            GitlabCommitEntity commit,
            String gerritChangeId) {
        this.assignment = assignment;
        this.commit = commit;
        this.gerritChangeId = gerritChangeId;
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public ReviewAssignmentEntity getAssignment() {
        return assignment;
    }

    public void setAssignment(ReviewAssignmentEntity assignment) {
        this.assignment = assignment;
    }

    public GitlabCommitEntity getCommit() {
        return commit;
    }

    public void setCommit(GitlabCommitEntity commit) {
        this.commit = commit;
    }

    public String getGerritChangeId() {
        return gerritChangeId;
    }

    public void setGerritChangeId(String gerritChangeId) {
        this.gerritChangeId = gerritChangeId;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public Instant getLastStatusAt() {
        return lastStatusAt;
    }
}