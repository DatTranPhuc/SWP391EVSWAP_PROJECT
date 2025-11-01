package evswap.swp391to4.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponse {
    
    private BigDecimal balance;
    private String currency;
    private List<TransactionSummary> recentTransactions;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionSummary {
        private Integer paymentId;
        private BigDecimal amount;
        private String type; // "topup", "reservation", "refund"
        private String status;
        private Instant paidAt;
        private String description;
    }
}
