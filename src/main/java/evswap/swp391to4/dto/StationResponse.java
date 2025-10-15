package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class StationResponse {
    private Integer stationId;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    // Nếu cần cho tìm gần vị trí, bổ sung thêm field:
    // private Double distance;
}
