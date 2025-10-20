package evswap.swp391to4.service;

import evswap.swp391to4.dto.NotificationDto;
import evswap.swp391to4.entity.Notification;
import evswap.swp391to4.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationDto> getRecentNotifications(Integer driverId) {
        return notificationRepository.findTop10ByDriverDriverIdOrderBySentAtDesc(driverId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(Integer driverId) {
        return notificationRepository.countUnreadByDriverId(driverId);
    }

    @Transactional
    public void markAsRead(Integer notificationId, Integer driverId) {
        Notification notification = notificationRepository
                .findByNotiIdAndDriverDriverId(notificationId, driverId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo"));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Integer driverId) {
        List<Notification> unreadNotifications = notificationRepository
                .findUnreadByDriverId(driverId);

        if (unreadNotifications.isEmpty()) {
            return;
        }

        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    private NotificationDto toDto(Notification notification) {
        Instant sentAt = notification.getSentAt();
        if (sentAt == null) {
            sentAt = Instant.now();
        }

        return NotificationDto.builder()
                .id(notification.getNotiId())
                .title(notification.getTitle())
                .type(notification.getType())
                .read(Boolean.TRUE.equals(notification.getIsRead()))
                .sentAt(sentAt)
                .build();
    }
}
