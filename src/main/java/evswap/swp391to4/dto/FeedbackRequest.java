package evswap.swp391to4.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequest {
    private Integer driverId;
    
    @NotNull(message = "Vui lòng chọn trạm")
    private Integer stationId;
    
    @NotNull(message = "Vui lòng chọn đánh giá")
    @Min(value = 1, message = "Đánh giá phải từ 1 đến 5")
    @Max(value = 5, message = "Đánh giá phải từ 1 đến 5")
    private Integer rating;
    
    @Size(max = 500, message = "Nhận xét không được quá 500 ký tự")
    private String comment;
}
