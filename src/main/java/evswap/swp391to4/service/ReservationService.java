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
import evswap.swp391to4.entity.SwapTransaction;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.SwapTransactionRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final DriverRepository driverRepo;
    private final StationRepository stationRepo;
    private final VehicleRepository vehicleRepository;
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

        // Lấy danh sách pin 'full' tại trạm, sau đó lọc theo SOH/SOC và vehicleType
        List<Battery> stationBatteries = batteryService.getAllBatteriesForStation(station);
        String vehicleType = vehicle.getVehicleType();

        long eligible = stationBatteries.stream()
                .filter(b -> "full".equalsIgnoreCase(b.getState()))
                .filter(b -> b.getSohPercent() != null && b.getSohPercent() >= 80)
                .filter(b -> b.getSocPercent() != null && b.getSocPercent() == 100)
                .filter(b -> vehicleType != null && vehicleType.equalsIgnoreCase(b.getVehicleType()))
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
                        reservation.getStatus(),
                        reservation.getIsInstantSwap() != null && reservation.getIsInstantSwap()
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
                        reservation.getStatus(),
                        reservation.getIsInstantSwap() != null && reservation.getIsInstantSwap()
                ))
                .sorted((a, b) -> {
                    // Sort: checked_in first, then instant swap, then by time
                    boolean aCheckedIn = "checked_in".equals(a.status());
                    boolean bCheckedIn = "checked_in".equals(b.status());
                    if (aCheckedIn != bCheckedIn) {
                        return aCheckedIn ? -1 : 1;
                    }
                    boolean aInstant = a.isInstantSwap();
                    boolean bInstant = b.isInstantSwap();
                    if (aInstant != bInstant) {
                        return aInstant ? -1 : 1;
                    }
                    return a.reservedStart().compareTo(b.reservedStart());
                })
                .toList();
    }

    /**
     * Lấy danh sách lịch đặt (scheduled reservations) - chỉ bao gồm pending, confirmed, checked_in
     */
    @Transactional(readOnly = true)
    public List<ReservationSummary> getScheduledReservations(Integer driverId) {
        return reservationRepo.findByDriverDriverIdOrderByReservedStartAsc(driverId).stream()
                .filter(reservation -> {
                    String status = reservation.getStatus();
                    return "pending".equalsIgnoreCase(status) 
                        || "confirmed".equalsIgnoreCase(status) 
                        || "checked_in".equalsIgnoreCase(status);
                })
                .map(reservation -> new ReservationSummary(
                        reservation.getReservationId(),
                        reservation.getStation().getName(),
                        reservation.getReservedStart(),
                        reservation.getStatus(),
                        reservation.getIsInstantSwap() != null && reservation.getIsInstantSwap()
                ))
                .toList();
    }

    /**
     * Lấy lịch sử đổi pin (swap history) - chỉ bao gồm completed và canceled
     */
    @Transactional(readOnly = true)
    public List<ReservationSummary> getSwapHistory(Integer driverId) {
        return reservationRepo.findByDriverDriverIdOrderByReservedStartAsc(driverId).stream()
                .filter(reservation -> {
                    String status = reservation.getStatus();
                    return "completed".equalsIgnoreCase(status) 
                        || "canceled".equalsIgnoreCase(status);
                })
                .sorted((a, b) -> {
                    // Sort descending by reservedStart (most recent first)
                    if (a.getReservedStart() == null && b.getReservedStart() == null) return 0;
                    if (a.getReservedStart() == null) return 1;
                    if (b.getReservedStart() == null) return -1;
                    return b.getReservedStart().compareTo(a.getReservedStart());
                })
                .map(reservation -> new ReservationSummary(
                        reservation.getReservationId(),
                        reservation.getStation().getName(),
                        reservation.getReservedStart(),
                        reservation.getStatus(),
                        reservation.getIsInstantSwap() != null && reservation.getIsInstantSwap()
                ))
                .toList();
    }

    /**
     * Kiểm tra xe có lịch đặt active không (pending, confirmed, hoặc checked_in)
     * @param vehicleId ID của xe
     * @return true nếu xe đã có lịch đặt active, false nếu chưa có
     */
    @Transactional(readOnly = true)
    public boolean hasActiveReservationForVehicle(Integer vehicleId) {
        List<Reservation> activeReservations = reservationRepo.findByVehicleVehicleId(vehicleId).stream()
                .filter(reservation -> {
                    String status = reservation.getStatus();
                    return "pending".equalsIgnoreCase(status) 
                        || "confirmed".equalsIgnoreCase(status) 
                        || "checked_in".equalsIgnoreCase(status);
                })
                .toList();
        return !activeReservations.isEmpty();
    }

    /**
     * Hủy tất cả lịch đặt active của một xe (dùng khi tạo instant swap)
     * @param vehicleId ID của xe
     */
    @Transactional
    public void cancelActiveReservationsForVehicle(Integer vehicleId) {
        List<Reservation> activeReservations = reservationRepo.findByVehicleVehicleId(vehicleId).stream()
                .filter(reservation -> {
                    String status = reservation.getStatus();
                    return "pending".equalsIgnoreCase(status) 
                        || "confirmed".equalsIgnoreCase(status) 
                        || "checked_in".equalsIgnoreCase(status);
                })
                .toList();

        for (Reservation reservation : activeReservations) {
            // Chỉ hủy scheduled reservations (không phải instant swap)
            if (reservation.getIsInstantSwap() == null || !reservation.getIsInstantSwap()) {
                cancelReservationWithRefund(reservation.getReservationId());
            }
        }
    }

    /**
     * Xác nhận reservation bởi staff
     * Cho phép xác nhận từ pending status (scheduled với cash/transfer)
     */
    @Transactional
    public void confirmReservation(Integer reservationId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));
        
        if (!"pending".equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xác nhận reservation đang pending");
        }
        
        // For instant swap that somehow got to pending (shouldn't happen), skip to checked_in
        if (reservation.getIsInstantSwap() != null && reservation.getIsInstantSwap()) {
            reservation.setStatus("checked_in");
            reservation.setCheckedInAt(Instant.now());
        } else {
            reservation.setStatus("confirmed");
        }
        
        reservationRepo.save(reservation);
    }

    /**
     * Check-in reservation. Cho phép check-in từ confirmed status.
     * Instant swap reservations có thể đã ở checked_in status và skip bước này.
     */
    @Transactional
    public void checkInReservation(Integer reservationId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));
        
        // Allow check-in from confirmed status, or if already checked_in (no-op for instant swap)
        if ("checked_in".equalsIgnoreCase(reservation.getStatus())) {
            // Already checked in (e.g., instant swap), no action needed
            return;
        }
        
        if (!"confirmed".equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalStateException("Chỉ có thể check-in reservation đã được xác nhận");
        }
        
        reservation.setStatus("checked_in");
        reservation.setCheckedInAt(Instant.now());
        reservationRepo.save(reservation);
    }
    
    /**
     * Tự động xác nhận reservation cho scheduled reservations đã thanh toán ví
     * (Được gọi tự động trong createReservationWithPaymentMethod, nhưng có thể gọi lại nếu cần)
     */
    @Transactional
    public void autoConfirmReservation(Integer reservationId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));
        
        // Only auto-confirm if it's a scheduled reservation with wallet payment that's still pending
        if (reservation.getIsInstantSwap() != null && reservation.getIsInstantSwap()) {
            // Instant swap should already be checked_in, skip
            return;
        }
        
        if (!"pending".equalsIgnoreCase(reservation.getStatus())) {
            // Already confirmed or checked_in
            return;
        }
        
        if (!"wallet".equalsIgnoreCase(reservation.getPaymentMethod())) {
            // Only auto-confirm wallet payments for scheduled reservations
            return;
        }
        
        if (!"completed".equalsIgnoreCase(reservation.getPaymentStatus())) {
            // Payment not completed yet
            return;
        }
        
        reservation.setStatus("confirmed");
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
     * Xóa reservation khỏi lịch sử (chỉ cho phép xóa completed hoặc canceled)
     * @param reservationId ID của reservation cần xóa
     * @param driverId ID của driver (để verify ownership)
     */
    @Transactional
    public void deleteReservationFromHistory(Integer reservationId, Integer driverId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));
        
        // Verify ownership
        if (!reservation.getDriver().getDriverId().equals(driverId)) {
            throw new IllegalStateException("Bạn không có quyền xóa đặt lịch này");
        }
        
        // Chỉ cho phép xóa completed hoặc canceled
        String status = reservation.getStatus();
        if (!"completed".equalsIgnoreCase(status) && !"canceled".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Chỉ có thể xóa lịch sử đã hoàn tất hoặc đã hủy");
        }
        
        reservationRepo.delete(reservation);
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

        // Kiểm tra payment status nếu không phải wallet payment
        if (!"wallet".equalsIgnoreCase(reservation.getPaymentMethod())) {
            if (!"completed".equalsIgnoreCase(reservation.getPaymentStatus())) {
                throw new IllegalStateException("Chưa xác nhận thanh toán. Vui lòng xác nhận thanh toán trước khi hoàn tất.");
            }
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
            String vehicleType = vehicle.getVehicleType();

            Battery chosen = stationBatteries.stream()
                    .filter(b -> "full".equalsIgnoreCase(b.getState()))
                    .filter(b -> b.getSohPercent() != null && b.getSohPercent() >= 80)
                    .filter(b -> b.getSocPercent() != null && b.getSocPercent() == 100)
                    .filter(b -> vehicleType != null && vehicleType.equalsIgnoreCase(b.getVehicleType()))
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

    /**
     * Lấy reservation theo QR token và validate
     * @param qrToken QR token của reservation
     * @param stationId ID của trạm (để validate reservation thuộc trạm của staff)
     * @return Reservation nếu hợp lệ
     * @throws IllegalStateException nếu token không hợp lệ
     */
    @Transactional(readOnly = true)
    public Reservation getReservationByQrToken(String qrToken, Integer stationId) {
        if (qrToken == null || qrToken.isBlank()) {
            throw new IllegalStateException("QR token không được để trống");
        }

        Reservation reservation = reservationRepo.findByQrToken(qrToken)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch với QR token này"));

        // Validate QR status
        if (!"active".equalsIgnoreCase(reservation.getQrStatus())) {
            throw new IllegalStateException("QR code không còn hoạt động. Trạng thái: " + reservation.getQrStatus());
        }

        // Validate QR chưa hết hạn
        if (reservation.getQrExpiresAt() != null && reservation.getQrExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("QR code đã hết hạn");
        }

        // Validate reservation status (phải là confirmed hoặc checked_in để có thể check-in)
        String status = reservation.getStatus();
        if (!"confirmed".equalsIgnoreCase(status) && !"checked_in".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Reservation chưa sẵn sàng để check-in. Trạng thái hiện tại: " + status);
        }

        // Validate reservation thuộc trạm của staff
        if (stationId != null && !reservation.getStation().getStationId().equals(stationId)) {
            throw new IllegalStateException("Reservation này không thuộc trạm của bạn");
        }

        return reservation;
    }

    /**
     * Tạo reservation với payment method tùy chọn (wallet/cash/transfer)
     * @param paymentMethod "wallet", "cash", hoặc "transfer"
     * @param isInstant true nếu là instant swap, false nếu scheduled
     */
    @Transactional
    public Reservation createReservationWithPaymentMethod(
            Integer driverId, 
            Integer stationId, 
            Integer vehicleId, 
            Instant reservedStart,
            String paymentMethod,
            Boolean isInstant) {
        
        // For instant swap, set reservedStart to now if not provided
        if (isInstant != null && isInstant && reservedStart == null) {
            reservedStart = Instant.now();
        }
        
        // For instant swap, validate that reservedStart is not in the past
        if (isInstant != null && isInstant && reservedStart != null && reservedStart.isBefore(Instant.now().minusSeconds(300))) {
            throw new IllegalStateException("Không thể đặt lịch instant swap cho thời gian trong quá khứ");
        }
        
        // For scheduled reservations, validate future time
        if (isInstant == null || !isInstant) {
            if (reservedStart != null && reservedStart.isBefore(Instant.now())) {
                throw new IllegalStateException("Thời gian đặt lịch phải ở tương lai");
            }
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

        // Nếu là instant swap, tự động hủy lịch đặt scheduled của xe đó
        if (isInstant != null && isInstant) {
            cancelActiveReservationsForVehicle(vehicleId);
        } else {
            // Nếu là scheduled reservation, kiểm tra xe đã có lịch đặt active chưa
            if (hasActiveReservationForVehicle(vehicleId)) {
                throw new IllegalStateException("Xe này đã có lịch đặt. Một xe chỉ có thể có một lịch đặt tại một thời điểm.");
            }
        }

        // Giá cố định: 25,000 VND
        java.math.BigDecimal price = new java.math.BigDecimal("25000");

        // Handle wallet payment
        if ("wallet".equalsIgnoreCase(paymentMethod)) {
            java.math.BigDecimal balance = walletService.getBalance(driverId);
            if (balance.compareTo(price) < 0) {
                throw new IllegalStateException("Số dư không đủ. Vui lòng nạp thêm tiền vào ví.");
            }
        }

        // Kiểm tra pin đủ điều kiện
        if (!hasEligibleBattery(stationId, vehicleId)) {
            throw new IllegalStateException("Trạm này hiện không có pin phù hợp với xe của bạn.");
        }

        // Determine initial status based on reservation type and payment method
        String initialStatus;
        if (isInstant != null && isInstant) {
            // Instant swap: go directly to checked_in (driver is already at station)
            initialStatus = "checked_in";
        } else if ("wallet".equalsIgnoreCase(paymentMethod)) {
            // Scheduled with wallet payment: auto-confirm after payment
            initialStatus = "confirmed";
        } else {
            // Scheduled with cash/transfer: keep pending until staff confirms payment
            initialStatus = "pending";
        }

        // Tạo reservation
        Reservation reservation = Reservation.builder()
                .driver(driver)
                .station(station)
                .vehicle(vehicle)
                .reservedStart(reservedStart)
                .priceAmount(price)
                .status(initialStatus)
                .paymentMethod(paymentMethod)
                .paymentStatus("wallet".equalsIgnoreCase(paymentMethod) ? "completed" : "pending")
                .isInstantSwap(isInstant != null && isInstant)
                .createdAt(Instant.now())
                .build();
        
        // Set checked_in_at for instant swap
        if (isInstant != null && isInstant && "checked_in".equals(initialStatus)) {
            reservation.setCheckedInAt(Instant.now());
        }
        
        reservation = reservationRepo.save(reservation);

        // Tạo QR code cho reservation
        generateQrCode(reservation);

        // Ghi nhận thanh toán ví nếu method là wallet
        if ("wallet".equalsIgnoreCase(paymentMethod)) {
            paymentService.createPayment(driver, reservation, price, "wallet", "succeed");
        }

        return reservation;
    }

    /**
     * Xác nhận thanh toán tiền mặt tại trạm
     */
    @Transactional
    public void confirmCashPayment(Integer reservationId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đặt lịch"));

        if (!"cash".equalsIgnoreCase(reservation.getPaymentMethod())) {
            throw new IllegalStateException("Reservation không phải thanh toán tiền mặt");
        }

        if ("completed".equalsIgnoreCase(reservation.getPaymentStatus())) {
            throw new IllegalStateException("Đã xác nhận thanh toán rồi");
        }

        reservation.setPaymentStatus("completed");
        reservationRepo.save(reservation);

        // Ghi nhận payment record
        paymentService.createPayment(reservation.getDriver(), reservation, reservation.getPriceAmount(), "cash", "succeed");
    }

    public record ReservationSummary(Integer reservationId,
                                     String stationName,
                                     Instant reservedStart,
                                     String status,
                                     Boolean isInstantSwap) {
    }
}
