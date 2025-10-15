package evswap.swp391to4.service;

import evswap.swp391to4.dto.NotificationRequest;
import evswap.swp391to4.dto.NotificationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Notification;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.NotificationRepository;
import evswap.swp391to4.repository.PaymentRepository;
import evswap.swp391to4.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DriverRepository driverRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản tài xế không tồn tại"));

        Reservation reservation = null;
        if (request.getReservationId() != null) {
            reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("Reservation không tồn tại"));
        }

        Payment payment = null;
        if (request.getPaymentId() != null) {
            payment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment không tồn tại"));
        }

        Notification notification = Notification.builder()
                .driver(driver)
                .type(request.getType())
                .title(request.getTitle())
                .isRead(false)
                .sentAt(Instant.now())
                .reservation(reservation)
                .payment(payment)
                .build();

        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    @Transactional
    public NotificationResponse markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification không tồn tại"));
        notification.setIsRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForDriver(Integer driverId) {
        return notificationRepository.findByDriverDriverIdOrderBySentAtDesc(driverId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(Integer driverId) {
        return notificationRepository.countByDriverDriverIdAndIsReadFalse(driverId);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotiId())
                .driverId(notification.getDriver() != null ? notification.getDriver().getDriverId() : null)
                .type(notification.getType())
                .title(notification.getTitle())
                .read(Boolean.TRUE.equals(notification.getIsRead()))
                .sentAt(notification.getSentAt())
                .reservationId(notification.getReservation() != null ? notification.getReservation().getReservationId() : null)
                .paymentId(notification.getPayment() != null ? notification.getPayment().getPaymentId() : null)
                .build();
    }
}
