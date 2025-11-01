package evswap.swp391to4.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketUpdateRequest {
    
    private String status;
    
    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    private String note;
    
    private Integer staffId;
}
