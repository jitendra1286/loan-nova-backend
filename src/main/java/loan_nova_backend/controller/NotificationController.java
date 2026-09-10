package loan_nova_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import loan_nova_backend.entity.Notification;
import loan_nova_backend.entity.User;
import loan_nova_backend.repository.UserRepository;
import loan_nova_backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationService notificationService,
            UserRepository userRepository) {

        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    private boolean isOwner(
            Long userId,
            String email) {

        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(email))
                .orElse(false);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserNotifications(
            @PathVariable Long userId,
            org.springframework.security.core.Authentication authentication) {

        if (!isOwner(userId, authentication.getName())) {

            return ResponseEntity.status(403)
                    .body("You are not authorized");
        }

        return ResponseEntity.ok(
                notificationService.getUserNotifications(userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<?> getUnreadCount(
            @PathVariable Long userId,
            org.springframework.security.core.Authentication authentication) {

        if (!isOwner(userId, authentication.getName())) {

            return ResponseEntity.status(403)
                    .body("You are not authorized");
        }

        return ResponseEntity.ok(
                notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {

        try {

            Notification notification =
                    notificationService.getNotificationById(id);

            if (!notification.getUser()
                    .getEmail()
                    .equals(authentication.getName())) {

                return ResponseEntity.status(403)
                        .body("You are not authorized");
            }

            return ResponseEntity.ok(
                    notificationService.markAsRead(id));

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<?> markAllAsRead(
            @PathVariable Long userId,
            org.springframework.security.core.Authentication authentication) {

        if (!isOwner(userId, authentication.getName())) {

            return ResponseEntity.status(403)
                    .body("You are not authorized");
        }

        notificationService.markAllAsRead(userId);

        return ResponseEntity.ok(
                "All notifications marked as read");
    }
    @PostMapping("/test/{userId}")
public ResponseEntity<?> createTestNotification(
        @PathVariable Long userId,
        @RequestBody Notification notification,
        org.springframework.security.core.Authentication authentication) {

    if (!isOwner(userId, authentication.getName())) {
        return ResponseEntity.status(403)
                .body("You are not authorized");
    }

    Notification saved =
            notificationService.createNotification(
                    userId,
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getType()
            );

    return ResponseEntity.ok(saved);
}
}