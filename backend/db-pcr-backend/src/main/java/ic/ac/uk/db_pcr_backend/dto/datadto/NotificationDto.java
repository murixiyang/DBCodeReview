package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.NotificationEntity;
import ic.ac.uk.db_pcr_backend.model.NotificationType;

public class NotificationDto {

    private NotificationType type;
    private String link;
    private String message;
    private boolean seen = false;
    private Instant createdAt;

    // Constructor
    public NotificationDto(NotificationType type, String link, String message, boolean seen, Instant createdAt) {
        this.type = type;
        this.link = link;
        this.message = message;
        this.seen = seen;
        this.createdAt = createdAt;
    }

    public static NotificationDto fromEntity(NotificationEntity entity) {
        return new NotificationDto(
                entity.getType(),
                entity.getLink(),
                entity.getMessage(),
                entity.isSeen(),
                entity.getCreatedAt());
    }

    // Getters and Setters

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}