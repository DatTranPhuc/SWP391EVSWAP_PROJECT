package evswap.swp391to4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOsWebhookRequest {
    private String code;
    private String desc;
    private PayOsData data;
    private String signature;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayOsData {
        private Long orderCode;
        // PayOS trả về paymentLinkId dạng chuỗi (ví dụ: "124c33293c43417ab7879e14c8d9eb18")
        private String paymentLinkId;
        private Long amount;
        private Long amountPaid;
        private Long amountRemaining;
        private String status;
        private String createdAt;
        private String transactions;
        private String cancelledAt;
        private String cancelledBy;
    }
}

