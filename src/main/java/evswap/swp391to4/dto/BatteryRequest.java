package evswap.swp391to4.dto;

import lombok.Data;

@Data
public class BatteryRequest {
    private Integer stationId;
    private String model;
    private String state;
    private Integer sohPercent;
    private Integer socPercent;
}
