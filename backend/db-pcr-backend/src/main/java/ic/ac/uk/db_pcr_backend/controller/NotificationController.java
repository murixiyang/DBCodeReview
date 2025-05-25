package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ic.ac.uk.db_pcr_backend.dto.datadto.NotificationDto;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;
import ic.ac.uk.db_pcr_backend.service.NotificationService;

@Controller
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notifSvc;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(@AuthenticationPrincipal OAuth2User oauth2User) {
        // Find user
        String username = oauth2User.getAttribute("username").toString();
        UserEntity currentUser = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        long cnt = notifSvc.countUnread(currentUser);
        return ResponseEntity.ok(cnt);
    }

    @GetMapping("/")
    public List<NotificationDto> list(@AuthenticationPrincipal OAuth2User oauth2User) {
        // Find user
        String username = oauth2User.getAttribute("username").toString();
        UserEntity currentUser = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return notifSvc.listAll(currentUser);
    }

    @PostMapping("/mark-read")
    public ResponseEntity<Void> markRead(
            @RequestParam("id") String id,
            @AuthenticationPrincipal OAuth2User oauth2User) {
        // Find user
        String username = oauth2User.getAttribute("username").toString();
        UserEntity currentUser = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        notifSvc.markRead(currentUser, Long.valueOf(id));
        return ResponseEntity.noContent().build();
    }

}
