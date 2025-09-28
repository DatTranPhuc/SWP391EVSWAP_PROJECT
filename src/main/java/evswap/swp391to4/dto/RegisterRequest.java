package evswap.swp391to4.dto;

import lombok.Data;

// DTO cho register
@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String phone;
}
