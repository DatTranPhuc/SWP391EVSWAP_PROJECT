package evswap.swp391to4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableBatteryResponse {
    
    private Integer batteryId;
    private String model;
    private Integer sohPercent;
    private Integer socPercent;
    private String state;
    private String stationName;
    private String stationAddress;
}
