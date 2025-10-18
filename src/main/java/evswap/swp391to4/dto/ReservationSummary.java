package evswap.swp391to4.dto;

import java.time.Instant;

/**
 * Lightweight projection of a reservation with the data required for driver dashboards.
 */
public record ReservationSummary(
        Integer reservationId,
        Integer stationId,
        String stationName,
        String stationAddress,
        Instant reservedStart,
        String status
) {
}
