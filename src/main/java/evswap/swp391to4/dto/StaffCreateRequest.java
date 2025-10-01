package evswap.swp391to4.dto;

import lombok.Data;

@Data
public class StaffCreateRequest {
    private String email;
    private String fullName;
    private String password;   // optional
    private Integer stationId; // optional
}
