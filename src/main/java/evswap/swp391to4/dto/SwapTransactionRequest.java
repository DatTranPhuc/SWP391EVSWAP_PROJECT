package evswap.swp391to4.dto;

import lombok.Data;

@Data
public class SwapTransactionRequest {
    private Integer reservationId;
    private Integer stationId;
    private Integer batteryOutId;
    private Integer batteryInId;
    private String result;
}
