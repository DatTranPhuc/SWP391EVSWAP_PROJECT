package evswap.swp391to4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private String email;
    private String fullName;
    private String message; // ví dụ: "Please verify your email"
}

