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
        return reservationRepo.save(reservation);
    }
}

