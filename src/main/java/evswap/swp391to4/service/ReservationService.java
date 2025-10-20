package evswap.swp391to4.service;

import evswap.swp391to4.dto.ReservationSummary;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final StationRepository stationRepository;

    private static final DateTimeFormatter RESERVATION_LABEL_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
                    .withZone(ZoneId.systemDefault());

    @Transactional
    public Reservation createReservation(Integer driverId, Integer stationId, Instant reservedStart) {
        Objects.requireNonNull(reservedStart, "Thời gian đặt lịch không hợp lệ");

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài xế với ID: " + driverId));

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạm với ID: " + stationId));

        Reservation reservation = Reservation.builder()
                .driver(driver)
                .station(station)
                .reservedStart(reservedStart)
                .status("pending")
                .createdAt(Instant.now())
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationSummary> getReservationsForDriver(Integer driverId) {
        return reservationRepository.findByDriver_DriverIdOrderByReservedStartAsc(driverId)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    private ReservationSummary toSummary(Reservation reservation) {
        Station station = reservation.getStation();
        Instant reservedStart = reservation.getReservedStart();
        String reservedStartLabel = null;
        if (reservedStart != null) {
            reservedStartLabel = RESERVATION_LABEL_FORMATTER.format(reservedStart);
        }

        return new ReservationSummary(
                reservation.getReservationId(),
                station != null ? station.getStationId() : null,
                station != null ? station.getName() : null,
                station != null ? station.getAddress() : null,
                reservedStart,
                reservedStartLabel,
                reservation.getStatus()
        );
    }
}
