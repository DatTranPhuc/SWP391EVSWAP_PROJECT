package evswap.swp391to4.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.VehicleBatteryCompatibilityRepository;
import evswap.swp391to4.repository.VehicleRepository;
import evswap.swp391to4.entity.SwapTransaction;
import evswap.swp391to4.repository.SwapTransactionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final DriverRepository driverRepo;
    private final StationRepository stationRepo;
    private final VehicleRepository vehicleRepository;
    private final VehicleBatteryCompatibilityRepository compatibilityRepository;
    private final PaymentService paymentService;
    private final WalletService walletService;
    private final BatteryService batteryService;
    private final SwapTransactionRepository swapTransactionRepository;

    @Transactional
    public Reservation createReservation(Integer driverId, Integer stationId, Instant reservedStart) {
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài xế"));

        Station station = stationRepo.findById(stationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạm"));

        Reservation reservation = Reservation.builder()
                .driver(driver)
                .station(station)
                .reservedStart(reservedStart)
                .status("pending")
                .createdAt(Instant.now())
                .build();

        return reservationRepo.save(reservation);
    }

    @Transactional(readOnly = true)
    public boolean hasEligibleBattery(Integer stationId, Integer vehicleId) {
        Station station = stationRepo.findById(stationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạm"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy xe"));
        // Ownership is validated at booking time; not needed for availability check

        // Lấy danh sách pin 'full' tại trạm, sau đó lọc theo SOH/SOC và tương thích
        List<Battery> stationBatteries = batteryService.getAllBatteriesForStation(station);
        java.util.Set<Integer> compatibleBatteryIds = compatibilityRepository.findByVehicle(vehicle)
                .stream().map(c -> c.getBattery().getBatteryId()).collect(java.util.stream.Collectors.toSet());

        long eligible = stationBatteries.stream()
                .filter(b -> "full".equalsIgnoreCase(b.getState()))
                .filter(b -> b.getSohPercent() != null && b.getSohPercent() >= 80)
                .filter(b -> b.getSocPercent() != null && b.getSocPercent() == 100)
                .filter(b -> compatibleBatteryIds.contains(b.getBatteryId()))
                .count();

        return eligible > 0;
    }

    @Transactional
    public Reservation createReservationWithPayment(Integer driverId, Integer stationId, Integer vehicleId, Instant reservedStart) {
        if (reservedStart.isBefore(Instant.now())) {
            throw new IllegalStateException("Thời gian đặt lịch phải ở tương lai");
        }

        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài xế"));
        Station station = stationRepo.findById(stationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạm"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy xe"));

        // Validate vehicle ownership
        if (!vehicle.getDriver().getDriverId().equals(driverId)) {
            throw new IllegalStateException("Xe không thuộc về tài xế này");
        }

        // Giá cố định: 25,000 VND
        java.math.BigDecimal price = new java.math.BigDecimal("25000");

        // Kiểm tra số dư ví (tính từ bảng payment)
        java.math.BigDecimal balance = walletService.getBalance(driverId);
        if (balance.compareTo(price) < 0) {
            throw new IllegalStateException("Số dư không đủ. Vui lòng nạp thêm tiền vào ví.");
        }

        // Kiểm tra pin đủ điều kiện (không giữ chỗ pin, chỉ đảm bảo có)
        if (!hasEligibleBattery(stationId, vehicleId)) {
            throw new IllegalStateException("Trạm này hiện không có pin phù hợp với xe của bạn.");
        }

        // Tạo reservation pending
        Reservation reservation = Reservation.builder()
                .driver(driver)
                .station(station)
                .vehicle(vehicle)
                .reservedStart(reservedStart)
                .priceAmount(price)
                .status("pending")
                .createdAt(Instant.now())
                .build();
        reservation = reservationRepo.save(reservation);

        // Tạo QR code cho reservation
        generateQrCode(reservation);

        // Ghi nhận thanh toán ví cho đặt lịch
        paymentService.createPayment(driver, reservation, price, "wallet", "succeed");

        return reservation;
    }

    @Transactional
    public void cancelReservationWithRefund(Integer reservationId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));

        if ("canceled".equalsIgnoreCase(reservation.getStatus()) ||
            "completed".equalsIgnoreCase(reservation.getStatus())) {
            return;
        }

        reservation.setStatus("canceled");
        reservationRepo.save(reservation);

        // Tính % hoàn tiền
        java.math.BigDecimal price = new java.math.BigDecimal("25000");
        java.math.BigDecimal refund;
        if (reservation.getReservedStart() != null && reservation.getReservedStart().isBefore(Instant.now())) {
            // Quá hạn chưa đến: hoàn 60%
            refund = price.multiply(new java.math.BigDecimal("0.6"));
        } else {
            // Hủy trước giờ: hoàn 100%
            refund = price;
        }

        paymentService.createPayment(reservation.getDriver(), reservation, refund, "wallet", "refunded");
    }

    @Transactional(readOnly = true)
    public List<ReservationSummary> getUpcomingReservations(Integer driverId) {
        Instant threshold = Instant.now().minus(1, ChronoUnit.HOURS);

        return reservationRepo.findByDriverDriverIdOrderByReservedStartAsc(driverId).stream()
                .filter(reservation -> reservation.getReservedStart() != null
                        && reservation.getReservedStart().isAfter(threshold))
                .map(reservation -> new ReservationSummary(
                        reservation.getReservationId(),
                        reservation.getStation().getName(),
                        reservation.getReservedStart(),
                        reservation.getStatus()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationSummary> getUpcomingReservationsForStation(Integer stationId) {
        Instant threshold = Instant.now().minus(1, ChronoUnit.HOURS);

        return reservationRepo.findByStationStationIdOrderByReservedStartAsc(stationId).stream()
                .filter(reservation -> reservation.getReservedStart() != null
                        && reservation.getReservedStart().isAfter(threshold))
                .map(reservation -> new ReservationSummary(
                        reservation.getReservationId(),
                        reservation.getStation().getName(),
                        reservation.getReservedStart(),
                        reservation.getStatus()
                ))
                .toList();
    }

    @Transactional
    public void confirmReservation(Integer reservationId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));
        
        if (!"pending".equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xác nhận reservation đang pending");
        }
        
        reservation.setStatus("confirmed");
        reservationRepo.save(reservation);
    }

    @Transactional
    public void checkInReservation(Integer reservationId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));
        
        if (!"confirmed".equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalStateException("Chỉ có thể check-in reservation đã được xác nhận");
        }
        
        reservation.setStatus("checked_in");
        reservation.setCheckedInAt(Instant.now());
        reservationRepo.save(reservation);
    }

    @Transactional
    public void reassignBattery(Integer reservationId, Integer newBatteryId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));
        
        Battery newBattery = batteryService.getBatteryById(newBatteryId);
        if (newBattery == null) {
            throw new IllegalStateException("Không tìm thấy pin mới");
        }
        
        // Validate battery eligibility
        if (!"full".equals(newBattery.getState()) || 
            newBattery.getSocPercent() != 100 || 
            newBattery.getSohPercent() < 80) {
            throw new IllegalStateException("Pin mới không đủ điều kiện");
        }
        
        reservation.setAssignedBattery(newBattery);
        reservationRepo.save(reservation);
    }

    /**
     * Hoàn tất đổi pin cho một reservation đã check-in.
     * - Trạng thái chuyển sang completed
     * - Nếu chưa có assignedBattery và staff không truyền batteryId, tự chọn pin đủ điều kiện đầu tiên tại trạm phù hợp với xe
     * - Đánh dấu QR đã sử dụng
     */
    @Transactional
    public void completeSwap(Integer reservationId, Integer assignedBatteryId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));

        if (!"checked_in".equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalStateException("Chỉ có thể hoàn tất khi đặt lịch đã check-in");
        }

        // Nếu staff truyền batteryId thì ưu tiên gán theo lựa chọn
        if (assignedBatteryId != null) {
            reassignBattery(reservationId, assignedBatteryId);
            // Reload entity để có pin mới
            reservation = reservationRepo.findById(reservationId)
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));
        }

        // Nếu vẫn chưa có pin được gán, tự chọn một pin đủ điều kiện
        if (reservation.getAssignedBattery() == null) {
            Station station = reservation.getStation();
            Vehicle vehicle = reservation.getVehicle();

            List<Battery> stationBatteries = batteryService.getAllBatteriesForStation(station);
            java.util.Set<Integer> compatibleBatteryIds = compatibilityRepository.findByVehicle(vehicle)
                    .stream().map(c -> c.getBattery().getBatteryId()).collect(java.util.stream.Collectors.toSet());

            Battery chosen = stationBatteries.stream()
                    .filter(b -> "full".equalsIgnoreCase(b.getState()))
                    .filter(b -> b.getSohPercent() != null && b.getSohPercent() >= 80)
                    .filter(b -> b.getSocPercent() != null && b.getSocPercent() == 100)
                    .filter(b -> compatibleBatteryIds.contains(b.getBatteryId()))
                    .findFirst()
                    .orElse(null);

            if (chosen == null) {
                throw new IllegalStateException("Không còn pin phù hợp để hoàn tất đổi pin");
            }

            reservation.setAssignedBattery(chosen);
        }

        // Cập nhật trạng thái hoàn tất và QR
        reservation.setStatus("completed");
        if (reservation.getQrStatus() != null && !"used".equalsIgnoreCase(reservation.getQrStatus())) {
            reservation.setQrStatus("used");
        }

        reservationRepo.save(reservation);

        // Ghi lại giao dịch đổi pin thành công
        SwapTransaction tx = swapTransactionRepository.findByReservation(reservation).orElse(null);
        if (tx == null) {
            tx = SwapTransaction.builder()
                    .reservation(reservation)
                    .station(reservation.getStation())
                    .swappedAt(Instant.now())
                    .result("success")
                    .build();
        } else {
            tx.setStation(reservation.getStation());
            tx.setSwappedAt(Instant.now());
            tx.setResult("success");
        }
        // Optional: map batteries if needed
        tx.setBatteryOut(reservation.getAssignedBattery());
        swapTransactionRepository.save(tx);
    }

    @Transactional
    private void generateQrCode(Reservation reservation) {
        // Generate QR token
        String qrToken = "QR_" + reservation.getReservationId() + "_" + System.currentTimeMillis();
        
        // Set QR expires in 24 hours
        Instant qrExpiresAt = Instant.now().plus(24, ChronoUnit.HOURS);
        
        reservation.setQrToken(qrToken);
        reservation.setQrStatus("active");
        reservation.setQrExpiresAt(qrExpiresAt);
        
        reservationRepo.save(reservation);
    }

    @Transactional(readOnly = true)
    public Reservation getReservationById(Integer reservationId) {
        return reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));
    }

    public record ReservationSummary(Integer reservationId,
                                     String stationName,
                                     Instant reservedStart,
                                     String status) {
    }
}
