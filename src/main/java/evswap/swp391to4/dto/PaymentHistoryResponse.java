package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentHistoryResponse {
    private String date;
    private String time;
    private String formattedAmount;
    private String status;
}