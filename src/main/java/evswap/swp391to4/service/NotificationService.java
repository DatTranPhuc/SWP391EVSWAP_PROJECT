package evswap.swp391to4.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final DriverRepository driverRepo;
    private final ReservationRepository reservationRepo;
    private final PaymentRepository paymentRepo;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByDriver(Integer driverId) {
        return notificationRepo.findByDriver_DriverIdOrderBySentAtDesc(driverId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotificationsByDriver(Integer driverId) {
        return notificationRepo.findByDriver_DriverIdAndIsReadOrderBySentAtDesc(driverId, false)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Integer driverId) {
        return notificationRepo.countUnreadByDriverId(driverId);
    }

    @Transactional
    public void markAsRead(Integer notiId, Integer driverId) {
        notificationRepo.markAsReadByIdAndDriverId(notiId, driverId);
    }

    @Transactional
    public void markAllAsRead(Integer driverId) {
        notificationRepo.markAllAsReadByDriverId(driverId);
    }

    @Transactional
    public Notification createNotification(Integer driverId, String type, String title, Integer reservationId, Integer paymentId) {
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài xế"));

        Notification.NotificationBuilder builder = Notification.builder()
                .driver(driver)
                .type(type)
                .title(title)
                .isRead(false)
                .sentAt(Instant.now());

        if (reservationId != null) {
            Reservation reservation = reservationRepo.findById(reservationId)
                    .orElse(null);
            builder.reservation(reservation);
        }

        if (paymentId != null) {
            Payment payment = paymentRepo.findById(paymentId)
                    .orElse(null);
            builder.payment(payment);
        }

        return notificationRepo.save(builder.build());
    }

    // Helper methods để tích hợp với các tính năng khác
    @Transactional
    public void notifyReservationCreated(Integer driverId, Integer reservationId, String stationName) {
        createNotification(
                driverId,
                "reservation_created",
                "Đặt lịch đổi pin tại " + stationName + " thành công",
                reservationId,
                null
        );
    }

    @Transactional
    public void notifyReservationConfirmed(Integer driverId, Integer reservationId, String stationName) {
        createNotification(
                driverId,
                "reservation_confirmed",
                "Đặt lịch tại " + stationName + " đã được xác nhận. Hãy đến trạm để check-in.",
                reservationId,
                null
        );
    }

    @Transactional
    public void notifyReservationCanceled(Integer driverId, Integer reservationId, String stationName) {
        createNotification(
                driverId,
                "reservation_canceled",
                "Đặt lịch tại " + stationName + " đã được hủy",
                reservationId,
                null
        );
    }

    @Transactional
    public void notifyPaymentSuccess(Integer driverId, Integer paymentId, Integer reservationId) {
        createNotification(
                driverId,
                "payment_success",
                "Thanh toán thành công cho đặt lịch của bạn",
                reservationId,
                paymentId
        );
    }

    @Transactional
    public void notifyPaymentFailed(Integer driverId, Integer paymentId, Integer reservationId) {
        createNotification(
                driverId,
                "payment_failed",
                "Thanh toán thất bại. Vui lòng thử lại.",
                reservationId,
                paymentId
        );
    }

    @Transactional
    public void notifyQrGenerated(Integer driverId, Integer reservationId, String stationName) {
        createNotification(
                driverId,
                "qr_generated",
                "Mã QR check-in đã sẵn sàng cho đặt lịch tại " + stationName,
                reservationId,
                null
        );
    }

    @Transactional
    public void notifyReservationReminder(Integer driverId, Integer reservationId, String stationName, Instant reservedStart) {
        createNotification(
                driverId,
                "reservation_reminder",
                "Nhắc nhở: Bạn có lịch đổi pin tại " + stationName + " sắp tới",
                reservationId,
                null
        );
    }

    @Transactional
    public void notifyTicketResponse(Integer driverId, Integer ticketId) {
        createNotification(
                driverId,
                "ticket_response",
                "Yêu cầu hỗ trợ của bạn đã có phản hồi mới",
                null,
                null
        );
    }

    @Transactional
    public void notifyFeedbackResponse(Integer driverId) {
        createNotification(
                driverId,
                "feedback_response",
                "Phản hồi của bạn đã được xem xét",
                null,
                null
        );
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse.NotificationResponseBuilder builder = NotificationResponse.builder()
                .notiId(notification.getNotiId())
                .type(notification.getType())
                .title(notification.getTitle())
                .isRead(Boolean.TRUE.equals(notification.getIsRead()))
                .sentAt(notification.getSentAt());

        if (notification.getReservation() != null) {
            builder.reservationId(notification.getReservation().getReservationId())
                   .reservationStatus(notification.getReservation().getStatus())
                   .stationName(notification.getReservation().getStation() != null 
                       ? notification.getReservation().getStation().getName() 
                       : null);
        }

        if (notification.getPayment() != null) {
            builder.paymentId(notification.getPayment().getPaymentId());
        }

        return builder.build();
    }

    @Transactional
    public void deleteReadNotifications(Integer driverId) {
        notificationRepo.deleteByDriver_DriverIdAndIsReadTrue(driverId);
    }
}

