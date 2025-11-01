package evswap.swp391to4.dto;

import java.time.Instant;

public record SwapSimulatorReservationDto(
        Integer reservationId,
        String status,
        Instant reservedStart,
        Instant createdAt,
        String driverName,
        String vehiclePlate,
        String assignedBatteryModel
) {
}
