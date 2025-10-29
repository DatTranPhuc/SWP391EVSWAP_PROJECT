package evswap.swp391to4.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final DriverRepository driverRepo;
    private final StationRepository stationRepo;
    private final NotificationService notificationService;

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

        Reservation saved = reservationRepo.save(reservation);
        
        // Gửi thông báo đặt lịch thành công
        try {
            notificationService.notifyReservationCreated(driverId, saved.getReservationId(), station.getName());
        } catch (Exception e) {
            // Log nhưng không ảnh hưởng đến luồng chính
            System.err.println("Failed to send notification: " + e.getMessage());
        }
        
        return saved;
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

    public record ReservationSummary(Integer reservationId,
                                     String stationName,
                                     Instant reservedStart,
                                     String status) {
    }
    
    @Transactional(readOnly = true)
    public Reservation getById(Integer reservationId) {
        return reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt lịch"));
    }

    @Transactional(readOnly = true)
    public Reservation findByIdOrThrow(Integer reservationId) {
        return getById(reservationId);
    }

    @Transactional
    public Reservation cancelReservation(Integer reservationId, Integer driverId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt lịch"));
        
        if (!reservation.getDriver().getDriverId().equals(driverId)) {
            throw new IllegalStateException("Bạn không có quyền hủy đặt lịch này");
        }
        
        String status = reservation.getStatus() != null ? reservation.getStatus().toLowerCase() : "";
        if ("completed".equals(status) || "canceled".equals(status) || "failed".equals(status)) {
            throw new IllegalStateException("Không thể hủy đặt lịch đã hoàn tất hoặc đã hủy");
        }
        
        if ("in_progress".equals(status)) {
            throw new IllegalStateException("Không thể hủy đặt lịch đang được xử lý");
        }
        
        reservation.setStatus("canceled");
        Reservation saved = reservationRepo.save(reservation);
        
        // Gửi thông báo hủy lịch
        try {
            notificationService.notifyReservationCanceled(driverId, saved.getReservationId(), saved.getStation().getName());
        } catch (Exception e) {
            // Log nhưng không ảnh hưởng đến luồng chính
            System.err.println("Failed to send notification: " + e.getMessage());
        }
        
        return saved;
    }
}

