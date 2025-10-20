package evswap.swp391to4.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StaffCreateRequest {

    @NotBlank(message = "Email không được để trống") // <-- LUẬT
    @Email(message = "Email không đúng định dạng")   // <-- LUẬT
    private String email;

    @NotBlank(message = "Họ tên không được để trống") // <-- LUẬT
    private String fullName;

    private String password;
    private Integer stationId;
}