package evswap.swp391to4.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCreateRequest {
    
    @NotNull(message = "Driver ID không được để trống")
    private Integer driverId;
    
    @NotNull(message = "Station ID không được để trống")
    private Integer stationId;
    
    @NotNull(message = "Vehicle ID không được để trống")
    private Integer vehicleId;
    
    @NotNull(message = "Thời gian đặt lịch không được để trống")
    private Instant reservedStart;
}
