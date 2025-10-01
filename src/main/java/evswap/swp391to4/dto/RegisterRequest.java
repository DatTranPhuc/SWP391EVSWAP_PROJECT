package evswap.swp391to4.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$",
            message = "Email phải có định dạng hợp lệ và kết thúc bằng .com"
    )
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Full name không được để trống")
    private String fullName;

    @Pattern(regexp = "^(\\+84|0)\\d{9}$", message = "Phone không hợp lệ")
    private String phone;
}
