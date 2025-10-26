package evswap.swp391to4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSupportRequest {
    
    @NotBlank(message = "Vui lòng chọn danh mục hỗ trợ")
    private String category;
    
    @NotBlank(message = "Vui lòng nhập mô tả vấn đề")
    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String comment;
}