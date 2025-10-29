package evswap.swp391to4.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Notification;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.entity.SwapTransaction;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.NotificationRepository;
import evswap.swp391to4.repository.PaymentRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.SwapTransactionRepository;
import evswap.swp391to4.repository.VehicleBatteryCompatibilityRepository;
import evswap.swp391to4.repository.VehicleRepository;
import evswap.swp391to4.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SwapService {

    private final ReservationRepository reservationRepo;
    private final SwapTransactionRepository swapRepo;
    private final PaymentRepository paymentRepo;
    private final BatteryRepository batteryRepo;
    private final NotificationRepository notificationRepo;
    private final VehicleRepository vehicleRepo;
    private final VehicleBatteryCompatibilityRepository compatibilityRepo;
    private final NotificationService notificationService;

    @Transactional
    public Reservation generateQrForReservation(Integer reservationId, Driver driver) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt lịch"));
        if (!reservation.getDriver().getDriverId().equals(driver.getDriverId())) {
            throw new IllegalStateException("Bạn không có quyền tạo QR cho đặt lịch này");
        }
        String nonce = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString() + ":" + nonce;
        reservation.setQrNonce(nonce);
        reservation.setQrToken(token);
        reservation.setQrStatus("active");
        // QR hết hạn vào thời gian đặt lịch (reservedStart) + 15 phút
        Instant expiryTime;
        if (reservation.getReservedStart() != null && reservation.getReservedStart().isAfter(Instant.now())) {
            // Nếu thời gian đặt lịch trong tương lai, QR hết hạn vào reservedStart + 15 phút
            expiryTime = reservation.getReservedStart().plus(15, ChronoUnit.MINUTES);
        } else {
            // Nếu thời gian đặt lịch đã qua hoặc null, QR hết hạn 15 phút từ bây giờ
            expiryTime = Instant.now().plus(15, ChronoUnit.MINUTES);
        }
        reservation.setQrExpiresAt(expiryTime);
        Reservation saved = reservationRepo.save(reservation);
        
        // Gửi thông báo QR đã được tạo
        try {
            notificationService.notifyQrGenerated(driver.getDriverId(), saved.getReservationId(), saved.getStation().getName());
        } catch (Exception e) {
            System.err.println("Failed to send QR notification: " + e.getMessage());
        }
        
        return saved;
    }

    @Transactional
    public Reservation checkInByQr(String qrToken, Staff staff) {
        if (qrToken == null || qrToken.isBlank()) {
            throw new IllegalArgumentException("QR không hợp lệ");
        }
        Reservation reservation = reservationRepo.findByQrToken(qrToken);
        if (reservation == null) {
            throw new IllegalArgumentException("QR không tồn tại");
        }
        if (reservation.getQrExpiresAt() == null || reservation.getQrExpiresAt().isBefore(Instant.now())) {
            reservation.setQrStatus("expired");
            reservationRepo.save(reservation);
            throw new IllegalStateException("QR đã hết hạn");
        }
        Station staffStation = staff.getStation();
        if (!reservation.getStation().getStationId().equals(staffStation.getStationId())) {
            throw new IllegalStateException("Đặt lịch không thuộc trạm của bạn");
        }
        if (reservation.getCheckedInAt() == null) {
            reservation.setCheckedInAt(Instant.now());
        }
        if ("completed".equalsIgnoreCase(reservation.getStatus()) || "canceled".equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalStateException("Đặt lịch đã kết thúc, không thể check-in");
        }
        reservation.setStatus("confirmed");
        reservation.setQrStatus("used");
        Reservation saved = reservationRepo.save(reservation);
        
        // Gửi thông báo xác nhận check-in
        try {
            notificationService.notifyReservationConfirmed(reservation.getDriver().getDriverId(), saved.getReservationId(), saved.getStation().getName());
        } catch (Exception e) {
            System.err.println("Failed to send check-in notification: " + e.getMessage());
        }
        
        return saved;
    }

    @Transactional
    public Reservation startSwap(Integer reservationId, Staff staff) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt lịch"));
        if (!reservation.getStation().getStationId().equals(staff.getStation().getStationId())) {
            throw new IllegalStateException("Đặt lịch không thuộc trạm của bạn");
        }
        if (reservation.getCheckedInAt() == null) {
            reservation.setCheckedInAt(Instant.now());
        }
        reservation.setStatus("in_progress");
        return reservationRepo.save(reservation);
    }

    @Transactional
    public SwapTransaction completeSwap(Integer reservationId, Integer batteryOutId, Integer batteryInId, String result, Staff staff) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt lịch"));
        if (!reservation.getStation().getStationId().equals(staff.getStation().getStationId())) {
            throw new IllegalStateException("Đặt lịch không thuộc trạm của bạn");
        }
        List<SwapTransaction> existing = swapRepo.findByReservation_Driver_DriverIdAndSwappedAtAfter(
                reservation.getDriver().getDriverId(), Instant.EPOCH);
        if (existing.stream().anyMatch(tx -> tx.getReservation().getReservationId().equals(reservationId))) {
            throw new IllegalStateException("Đặt lịch đã có giao dịch");
        }

        Battery batteryOut = null;
        Battery batteryIn = null;
        if (batteryOutId != null) {
            batteryOut = batteryRepo.findById(batteryOutId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy pin trả"));
        }
        if (batteryInId != null) {
            batteryIn = batteryRepo.findById(batteryInId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy pin nhận"));
        }
        // Rule: chỉ cần cùng model là được (nếu cả hai được chọn)
        if (batteryOut != null && batteryIn != null) {
            String outModel = batteryOut.getModel() != null ? batteryOut.getModel().trim() : "";
            String inModel = batteryIn.getModel() != null ? batteryIn.getModel().trim() : "";
            if (!outModel.equalsIgnoreCase(inModel)) {
                throw new IllegalStateException("Pin nhận và pin trả phải cùng model");
            }
        }
        SwapTransaction tx = SwapTransaction.builder()
                .reservation(reservation)
                .station(reservation.getStation())
                .batteryOut(batteryOut)
                .batteryIn(batteryIn)
                .swappedAt(Instant.now())
                .result(result != null ? result : "success")
                .build();
        SwapTransaction saved = swapRepo.save(tx);

        // Cập nhật tồn kho pin và trạng thái đặt lịch theo kết quả
        String normalized = saved.getResult() != null ? saved.getResult().toLowerCase() : "success";
        if ("success".equals(normalized)) {
            if (batteryOut != null) {
                batteryOut.setState("charging");
                batteryOut.setStation(reservation.getStation());
                batteryRepo.save(batteryOut);
            }
            if (batteryIn != null) {
                batteryIn.setState("in_use");
                batteryIn.setStation(reservation.getStation());
                batteryRepo.save(batteryIn);
            }
            reservation.setStatus("completed");
            reservationRepo.save(reservation);
        } else if ("failed".equals(normalized)) {
            reservation.setStatus("failed");
            reservationRepo.save(reservation);
        } else if ("aborted".equals(normalized)) {
            reservation.setStatus("canceled");
            reservationRepo.save(reservation);
        }

        // Gửi thông báo cho tài xế về kết quả đổi pin
        String viTitle;
        switch (normalized) {
            case "success" -> viTitle = "Đổi pin thành công tại " + reservation.getStation().getName();
            case "failed" -> viTitle = "Đổi pin thất bại tại " + reservation.getStation().getName();
            case "aborted" -> viTitle = "Giao dịch đổi pin đã hủy tại " + reservation.getStation().getName();
            default -> viTitle = "Cập nhật kết quả đổi pin";
        }
        Notification noti = Notification.builder()
                .driver(reservation.getDriver())
                .type("swap_result")
                .title(viTitle)
                .isRead(false)
                .sentAt(Instant.now())
                .reservation(reservation)
                .build();
        notificationRepo.save(noti);

        return saved;
    }

    @Transactional
    public void recordCashPaymentIfNeeded(Integer reservationId, Driver driver) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt lịch"));
        if (!reservation.getDriver().getDriverId().equals(driver.getDriverId())) {
            throw new IllegalStateException("Bạn không có quyền thanh toán cho đặt lịch này");
        }
        boolean hasSucceed = paymentRepo.findByReservation_ReservationId(reservationId).stream()
                .anyMatch(p -> "succeed".equalsIgnoreCase(p.getStatus()));
        if (hasSucceed) {
            return;
        }
        Payment payment = Payment.builder()
                .driver(driver)
                .reservation(reservation)
                .status("succeed")
                .method("cash")
                .paidAt(Instant.now())
                .currency("VND")
                .amount(BigDecimal.ZERO)
                .build();
        paymentRepo.save(payment);
    }
}


