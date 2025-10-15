package evswap.swp391to4.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class StationUpdateRequest {
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
}
