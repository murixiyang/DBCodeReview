package ic.ac.uk.db_pcr_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.dto.datadto.NotificationDto;
import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.NotificationEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.NotificationType;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.NotificationRepo;
import jakarta.transaction.Transactional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    /** Count unread notifications for the given user. */
    public long countUnread(UserEntity user) {
        System.out.println("Service: NotificationService.countUnread");

        return notificationRepo.countByRecipientAndSeenFalse(user);
    }

    /** List *all* notifications (most‐recent first) for the given user. */
    public List<NotificationDto> listAll(UserEntity user) {
        System.out.println("Service: NotificationService.listAll");

        return notificationRepo.findByRecipientOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /** Mark a single notification as read. */
    @Transactional
    public void markRead(UserEntity user, Long notificationId) {
        System.out.println("Service: NotificationService.markRead");

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
        System.out.println("Service: NotificationService.sendNotification");

        NotificationEntity e = new NotificationEntity(recipient, type, link, message);

        notificationRepo.save(e);
    }

    // Send new submission notification --> to every reviewer
    public void sendNewSubmissionNotification(String gerritChangeId, List<ReviewAssignmentEntity> assignments) {
        System.out.println("Service: NotificationService.sendNewSubmissionNotification");

        List<NotificationEntity> notifications = new ArrayList<>();

        assignments.forEach(assignment -> {
            UserEntity reviewer = assignment.getReviewer();
            NotificationType type = NotificationType.NEW_SUBMISSION;
            String link = "review/detail/" + gerritChangeId;
            String message = "New submission for project: " + assignment.getGroupProject().getName();

            notifications.add(new NotificationEntity(reviewer, type, link, message));
        });

        notificationRepo.saveAll(notifications);
    }

}
