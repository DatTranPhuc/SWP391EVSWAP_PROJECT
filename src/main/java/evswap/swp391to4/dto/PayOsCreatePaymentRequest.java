package evswap.swp391to4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOsCreatePaymentRequest {
    private Long orderCode;
    private Long amount;
    private String description;
    private String webhookUrl;
    private String cancelUrl;
    private String successUrl;
}

