package evswap.swp391to4.service;

import evswap.swp391to4.dto.ReservationRequest;
import evswap.swp391to4.dto.ReservationResponse;
import evswap.swp391to4.dto.ReservationStatusUpdateRequest;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final StationRepository stationRepository;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản tài xế không tồn tại"));
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));

        Reservation reservation = Reservation.builder()
                .driver(driver)
                .station(station)
                .reservedStart(request.getReservedStart())
                .status("pending")
                .createdAt(Instant.now())
                .qrStatus("inactive")
                .build();

        Reservation saved = reservationRepository.save(reservation);
        return toResponse(saved);
    }

    @Transactional
    public ReservationResponse updateStatus(Integer reservationId, ReservationStatusUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation không tồn tại"));

        if (request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
            if ("completed".equalsIgnoreCase(request.getStatus())) {
                reservation.setCheckedInAt(request.getCheckedInAt() != null ? request.getCheckedInAt() : Instant.now());
                reservation.setQrStatus("used");
            }
            if ("canceled".equalsIgnoreCase(request.getStatus()) || "no_show".equalsIgnoreCase(request.getStatus())) {
                reservation.setQrStatus("revoked");
            }
        }

        if (request.getCheckedInAt() != null) {
            reservation.setCheckedInAt(request.getCheckedInAt());
        }

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse refreshQrCode(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation không tồn tại"));

        if (!"confirmed".equalsIgnoreCase(reservation.getStatus())
                && !"pending".equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalStateException("Chỉ tạo QR cho reservation pending hoặc confirmed");
        }

        reservation.setQrNonce(UUID.randomUUID().toString());
        reservation.setQrToken(UUID.randomUUID().toString().replace("-", ""));
        reservation.setQrExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        reservation.setQrStatus("active");

        Reservation saved = reservationRepository.save(reservation);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsForDriver(Integer driverId) {
        return reservationRepository.findByDriverDriverIdOrderByReservedStartDesc(driverId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsForStation(Integer stationId, Instant start, Instant end) {
        return reservationRepository.findByStationStationIdAndReservedStartBetween(stationId, start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .reservationId(reservation.getReservationId())
                .driverId(reservation.getDriver() != null ? reservation.getDriver().getDriverId() : null)
                .driverName(reservation.getDriver() != null ? reservation.getDriver().getFullName() : null)
                .stationId(reservation.getStation() != null ? reservation.getStation().getStationId() : null)
                .stationName(reservation.getStation() != null ? reservation.getStation().getName() : null)
                .reservedStart(reservation.getReservedStart())
                .createdAt(reservation.getCreatedAt())
                .status(reservation.getStatus())
                .qrToken(reservation.getQrToken())
                .qrStatus(reservation.getQrStatus())
                .qrExpiresAt(reservation.getQrExpiresAt())
                .checkedInAt(reservation.getCheckedInAt())
                .build();
    }
}
