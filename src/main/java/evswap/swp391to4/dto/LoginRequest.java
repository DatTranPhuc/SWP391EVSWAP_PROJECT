package evswap.swp391to4.dto;

import lombok.Data;

// DTO cho login
@Data
public class LoginRequest {
    private String email;
    private String password;
}
