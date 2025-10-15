// File: evswap/swp391to4/dto/StaffResponse.java
package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffResponse {
    private Integer staffId;
    private String email;
    private String fullName;
    private Boolean isActive;
    private Integer stationId;
    private String stationName; // <-- Thêm trường này
    private String status;      // <-- Thêm trường này
}