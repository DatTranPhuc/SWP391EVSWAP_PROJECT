package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SwapTransactionResponse {
    private Integer swapId;
    private Integer reservationId;
    private Integer stationId;
    private String stationName;
    private Integer batteryOutId;
    private Integer batteryInId;
    private Instant swappedAt;
    private String result;
}
