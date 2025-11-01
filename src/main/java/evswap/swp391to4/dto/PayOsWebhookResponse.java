package evswap.swp391to4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOsWebhookResponse {
    private Integer code;
    private String desc;
    private PayOsData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayOsData {
        private Long orderCode;
        private String providerTransactionCode;
    }
}

