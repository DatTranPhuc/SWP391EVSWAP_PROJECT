package evswap.swp391to4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationResponse {
    private Integer stationId;
    private String name;
    private String address;
    private String status;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double distance; // nullable tuỳ hiển thị
}