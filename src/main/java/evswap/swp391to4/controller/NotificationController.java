package evswap.swp391to4.controller;

import evswap.swp391to4.dto.NotificationRequest;
import evswap.swp391to4.dto.NotificationResponse;
import evswap.swp391to4.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> send(@RequestBody NotificationRequest request) {
        return ResponseEntity.status(201).body(notificationService.sendNotification(request));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Integer notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<NotificationResponse>> listForDriver(@PathVariable Integer driverId) {
        return ResponseEntity.ok(notificationService.getNotificationsForDriver(driverId));
    }

    @GetMapping("/driver/{driverId}/unread-count")
    public ResponseEntity<Long> unreadCount(@PathVariable Integer driverId) {
        return ResponseEntity.ok(notificationService.countUnread(driverId));
    }
}
