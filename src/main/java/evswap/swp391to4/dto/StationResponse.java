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

    // Cần cho việc hiển thị trên bản đồ
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Cần cho việc hiển thị khoảng cách trong bảng
    private Double distance;
}