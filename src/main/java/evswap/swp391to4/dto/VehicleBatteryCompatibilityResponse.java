package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleBatteryCompatibilityResponse {
    private Integer vehicleId;
    private Integer batteryId;
    private String vehicleModel;
    private String batteryModel;
}
