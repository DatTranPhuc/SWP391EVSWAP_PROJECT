package evswap.swp391to4.service;

import evswap.swp391to4.entity.Reservation;

import java.time.Instant;
import java.util.List;

public interface ReservationService {
    Reservation createReservation(Integer driverId, Integer stationId, Instant reservedStart);
    List<ReservationSummary> getUpcomingReservations(Integer driverId);

    record ReservationSummary(Integer reservationId, String stationName, Instant reservedStart, String status) { }
}
