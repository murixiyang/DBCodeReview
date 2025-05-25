package ic.ac.uk.db_pcr_backend.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import ic.ac.uk.db_pcr_backend.model.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    @GeneratedValue
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity recipient;

    @Column(nullable = false, length = 30)
    private NotificationType type; // e.g. "CHANGES_REQUESTED", "REPLY_POSTED", etc.

    @Column(nullable = false)
    private String link; // URL path within your SPA, e.g. "/review/detail/123"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message; // human‐readable text

    @Column(nullable = false)
    private boolean seen = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // Constructor
    public NotificationEntity() {
    }

    public NotificationEntity(UserEntity recipient, NotificationType type, String link, String message) {
        this.recipient = recipient;
        this.type = type;
        this.link = link;
        this.message = message;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public UserEntity getRecipient() {
        return recipient;
    }

    public void setRecipient(UserEntity recipient) {
        this.recipient = recipient;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
