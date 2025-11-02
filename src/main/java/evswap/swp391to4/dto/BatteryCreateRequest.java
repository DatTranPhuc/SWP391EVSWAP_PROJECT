package evswap.swp391to4.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryCreateRequest {

    @NotBlank(message = "Model không được để trống")
    private String model;

    @NotBlank(message = "Loại xe không được để trống")
    private String vehicleType; // motorcycle/car

    @NotBlank(message = "Trạng thái ban đầu không được để trống")
    private String state;

    @NotNull(message = "SOH không được để trống")
    @Min(value = 0, message = "SOH tối thiểu là 0")
    @Max(value = 100, message = "SOH tối đa là 100")
    private Integer sohPercent;

    @NotNull(message = "SOC không được để trống")
    @Min(value = 0, message = "SOC tối thiểu là 0")
    @Max(value = 100, message = "SOC tối đa là 100")
    private Integer socPercent;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Phải thêm ít nhất 1 pin")
    @Max(value = 100, message = "Chỉ có thể thêm tối đa 100 pin cùng lúc")
    private Integer quantity;
}