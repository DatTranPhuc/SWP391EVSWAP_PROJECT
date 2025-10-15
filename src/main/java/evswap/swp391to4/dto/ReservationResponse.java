package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ReservationResponse {
    private Integer reservationId;
    private Integer driverId;
    private String driverName;
    private Integer stationId;
    private String stationName;
    private Instant reservedStart;
    private Instant createdAt;
    private String status;
    private String qrToken;
    private String qrStatus;
    private Instant qrExpiresAt;
    private Instant checkedInAt;
}
