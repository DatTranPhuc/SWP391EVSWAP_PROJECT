package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatteryResponse {
    private Integer batteryId;
    private Integer stationId;
    private String stationName;
    private String model;
    private String state;
    private Integer sohPercent;
    private Integer socPercent;
}
