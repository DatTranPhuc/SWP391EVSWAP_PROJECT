package evswap.swp391to4.dto;

import lombok.Data;

@Data
public class BatteryUpdateRequest {
    private Integer stationId;
    private String state;
    private Integer sohPercent;
    private Integer socPercent;
}
