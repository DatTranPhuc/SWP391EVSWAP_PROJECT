package evswap.swp391to4.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ReservationRequest {
    private Integer driverId;
    private Integer stationId;
    private Instant reservedStart;
}
