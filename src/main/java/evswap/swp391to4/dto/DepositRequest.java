package evswap.swp391to4.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO này đại diện cho dữ liệu trong form "Nạp tiền" mà người dùng gửi lên.
 * Nó chứa các quy tắc xác thực (validation) để đảm bảo dữ liệu đầu vào là hợp lệ.
 */
@Data
public class DepositRequest {

    @NotNull(message = "Số tiền không được để trống.")
    @DecimalMin(value = "10000.0", inclusive = true, message = "Số tiền nạp tối thiểu là 10,000đ.")
    private BigDecimal amount;
}