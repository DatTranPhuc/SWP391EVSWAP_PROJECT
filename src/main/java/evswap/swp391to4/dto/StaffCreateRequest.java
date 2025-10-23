package evswap.swp391to4.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // <-- THÊM IMPORT NÀY
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StaffCreateRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String password;

    @NotNull(message = "Bạn phải chọn một trạm cho nhân viên")
    @Positive(message = "Station ID phải là số dương")
    private Integer stationId;
}