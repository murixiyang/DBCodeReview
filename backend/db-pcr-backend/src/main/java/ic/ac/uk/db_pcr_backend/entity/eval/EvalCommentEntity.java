package ic.ac.uk.db_pcr_backend.entity.eval;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.ReactState;
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
@Table(name = "eval_comments", uniqueConstraints = @UniqueConstraint(columnNames = { "gerrit_change_id",
        "gerrit_comment_id" }))
public class EvalCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gerrit_change_id", nullable = false, length = 64)
    private String gerritChangeId;

    /** The raw Gerrit comment ID (returned by Gerrit on create/update) */
    @Column(name = "gerrit_comment_id", nullable = false, length = 64)
    private String gerritCommentId;

    /** Who really wrote the comment (your GitLab user) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_user"))
    private UserEntity commentUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "thumb_state", length = 10, nullable = false)
    private ReactState thumbState = ReactState.NONE;

    /** Timestamps */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // --- Constructors ---

    public EvalCommentEntity() {
    }

    public EvalCommentEntity(
            String gerritChangeId,
            String gerritCommentId,
            UserEntity commentUser) {
        this.gerritChangeId = gerritChangeId;
        this.gerritCommentId = gerritCommentId;
        this.commentUser = commentUser;
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGerritChangeId() {
        return gerritChangeId;
    }

    public void setGerritChangeId(String gerritChangeId) {
        this.gerritChangeId = gerritChangeId;
    }

    public String getGerritCommentId() {
        return gerritCommentId;
    }

    public void setGerritCommentId(String gerritCommentId) {
        this.gerritCommentId = gerritCommentId;
    }

    public UserEntity getCommentUser() {
        return commentUser;
    }

    public void setCommentUser(UserEntity commentUser) {
        this.commentUser = commentUser;
    }

    public ReactState getThumbState() {
        return thumbState;
    }

    public void setThumbState(ReactState thumbState) {
        this.thumbState = thumbState;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
