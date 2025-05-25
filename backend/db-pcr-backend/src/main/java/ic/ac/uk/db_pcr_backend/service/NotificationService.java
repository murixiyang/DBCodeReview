package ic.ac.uk.db_pcr_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.dto.datadto.NotificationDto;
import ic.ac.uk.db_pcr_backend.entity.NotificationEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.NotificationType;
import ic.ac.uk.db_pcr_backend.repository.NotificationRepo;
import jakarta.transaction.Transactional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    /** Count unread notifications for the given user. */
    public long countUnread(UserEntity user) {
        return notificationRepo.countByRecipientAndSeenFalse(user);
    }

    /** List *all* notifications (most‐recent first) for the given user. */
    public List<NotificationDto> listAll(UserEntity user) {
        return notificationRepo.findByRecipientOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /** Mark a single notification as read. */
    @Transactional
    public void markRead(UserEntity user, Long notificationId) {
        NotificationEntity n = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification not found: " + notificationId));
        if (!n.getRecipient().equals(user)) {
            throw new SecurityException("Cannot mark another user’s notification");
        }
        n.setSeen(true);
        // flush via transaction commit
    }

    public void sendNotification(UserEntity recipient,
            NotificationType type,
            String link,
            String message) {
        NotificationEntity e = new NotificationEntity(recipient, type, link, message);

        notificationRepo.save(e);
    }

}
