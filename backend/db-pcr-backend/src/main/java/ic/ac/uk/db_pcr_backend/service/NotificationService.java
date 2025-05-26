package ic.ac.uk.db_pcr_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gitlab4j.api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.dto.datadto.NotificationDto;
import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.NotificationEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.NotificationType;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GerritCommentRepo;
import ic.ac.uk.db_pcr_backend.repository.NotificationRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Autowired
    private GerritCommentRepo commentRepo;

    /** Count unread notifications for the given user. */
    @Transactional(readOnly = true)
    public long countUnread(UserEntity user) {
        System.out.println("Service: NotificationService.countUnread");

        return notificationRepo.countByRecipientAndSeenFalse(user);
    }

    /** List *all* notifications (most‐recent first) for the given user. */
    @Transactional(readOnly = true)
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

    @Transactional
    public void sendNotification(UserEntity recipient,
            NotificationType type,
            String link,
            String message) {
        System.out.println("Service: NotificationService.sendNotification");

        NotificationEntity e = new NotificationEntity(recipient, type, link, message);

        notificationRepo.save(e);
    }

    // Send new submission notification --> to every reviewer
    @Transactional
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

    // Send new comment notification --> to the author
    @Transactional
    public void sendNewCommentNotification(String gerritChangeId, Long assignmentId) {
        System.out.println("Service: NotificationService.sendNewCommentNotification");

        // Find Assignment
        ReviewAssignmentEntity assignment = reviewAssignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        UserEntity author = assignment.getAuthor();
        NotificationType type = NotificationType.NEW_COMMENT;
        String link = "author/detail/" + gerritChangeId;
        String message = "New comment on your submission for project: "
                + assignment.getGroupProject().getName();

        NotificationEntity notification = new NotificationEntity(author, type, link, message);
        notificationRepo.save(notification);
    }

    // Send change request notification --> to the author
    @Transactional
    public void sendChangeRequestNotification(String gerritChangeId, Long assignmentId) {
        System.out.println("Service: NotificationService.sendChangeRequesetNotification");

        // Find Assignment
        ReviewAssignmentEntity assignment = reviewAssignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        UserEntity author = assignment.getAuthor();
        NotificationType type = NotificationType.CHANGES_REQUESTED;
        String link = "author/detail/" + gerritChangeId;
        String message = "New change request on your submission for project: "
                + assignment.getGroupProject().getName();

        NotificationEntity notification = new NotificationEntity(author, type, link, message);
        notificationRepo.save(notification);
    }

    // Send approved notification --> to the author
    @Transactional
    public void sendApprovedNotification(String gerritChangeId, Long assignmentId) {
        System.out.println("Service: NotificationService.sendApprovedNotification");

        // Find Assignment
        ReviewAssignmentEntity assignment = reviewAssignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        UserEntity author = assignment.getAuthor();
        NotificationType type = NotificationType.APPROVED;
        String link = "author/detail/" + gerritChangeId;
        String message = "New approval on your submission for project: "
                + assignment.getGroupProject().getName();

        NotificationEntity notification = new NotificationEntity(author, type, link, message);
        notificationRepo.save(notification);
    }

    // Send new reply notification --> to the commenter (receiver is reviewer)
    @Transactional
    public void sendNewReplyNotificationToReviewer(String gerritChangeId, List<String> repliedCommentId,
            Long assignmentId) {
        System.out.println("Service: NotificationService.sendNewReplyNotificationToReviewer");

        // Find the users got replied
        Set<UserEntity> usersReplied = commentRepo.findByGerritCommentIdIn(repliedCommentId).stream()
                .map(comment -> comment.getCommentUser())
                .collect(Collectors.toSet());

        // Find Assignment
        ReviewAssignmentEntity assignment = reviewAssignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        // Filter out the author of the assignment
        usersReplied.remove(assignment.getAuthor());

        List<NotificationEntity> notifications = new ArrayList<>();

        usersReplied.forEach(recipient -> {
            NotificationType type = NotificationType.NEW_REPLY;
            String link = "review/detail/" + gerritChangeId;
            String message = "New reply for project: " + assignment.getGroupProject().getName();

            notifications.add(new NotificationEntity(recipient, type, link, message));
        });

        notificationRepo.saveAll(notifications);

    }

    @Transactional(readOnly = true)
    public boolean isReplyToAuthor(String gerritChangeId, List<String> repliedCommentId,
            Long assignmentId) {
        System.out.println("Service: NotificationService.isReplyToAuthor");

        // Find the users got replied
        Set<UserEntity> usersReplied = commentRepo.findByGerritCommentIdIn(repliedCommentId).stream()
                .map(comment -> comment.getCommentUser())
                .collect(Collectors.toSet());

        // Find Assignment
        ReviewAssignmentEntity assignment = reviewAssignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        return usersReplied.contains(assignment.getAuthor());
    }

    // Send new reply notification --> to the commenter (receiver is author)
    @Transactional
    public void sendNewReplyNotificationToAuthor(String gerritChangeId, Long assignmentId) {
        System.out.println("Service: NotificationService.sendNewReplyNotificationToAuthor");

        // Find Assignment
        ReviewAssignmentEntity assignment = reviewAssignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        UserEntity author = assignment.getAuthor();

        NotificationType type = NotificationType.NEW_REPLY;
        String link = "author/detail/" + gerritChangeId;
        String message = "New reply for project: " + assignment.getGroupProject().getName();

        NotificationEntity notification = new NotificationEntity(author, type, link, message);
        notificationRepo.save(notification);

    }

}
