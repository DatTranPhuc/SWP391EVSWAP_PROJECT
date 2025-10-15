package evswap.swp391to4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private Integer driverId;
    private String email;
    private String fullName;
    private String token; // nếu dùng JWT
    private String message; // ví dụ: "Login successful"
}

