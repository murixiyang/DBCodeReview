package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ic.ac.uk.db_pcr_backend.dto.datadto.NotificationDto;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.service.NotificationService;

@Controller
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notifSvc;

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(@AuthenticationPrincipal UserEntity currentUser) {
        long cnt = notifSvc.countUnread(currentUser);
        return ResponseEntity.ok(cnt);
    }

    @GetMapping("/")
    public List<NotificationDto> list(@AuthenticationPrincipal UserEntity currentUser) {
        return notifSvc.listAll(currentUser);
    }

    @PostMapping("/{id}/mark-read")
    public ResponseEntity<Void> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserEntity me) {
        notifSvc.markRead(me, id);
        return ResponseEntity.noContent().build();
    }

}
