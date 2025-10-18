package evswap.swp391to4.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StationCreateRequest {
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status; // "active" hoặc "closed"
}
