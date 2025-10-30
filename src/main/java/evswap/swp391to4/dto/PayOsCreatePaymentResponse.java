package evswap.swp391to4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOsCreatePaymentResponse {
    private Integer code;
    private String desc;
    private PayOsData data;
    private String signature;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayOsData {
        private Long paymentLinkId;
        private String checkoutUrl;
        private String qrCode;
        private Long orderCode;
        private Long amount;
    }
}

