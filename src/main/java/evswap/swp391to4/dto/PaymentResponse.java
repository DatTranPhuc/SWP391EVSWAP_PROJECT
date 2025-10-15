package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentResponse {
    private Integer paymentId;
    private Integer driverId;
    private String driverName;
    private Integer reservationId;
    private BigDecimal amount;
    private String method;
    private String status;
    private Instant paidAt;
    private String currency;
    private String providerTxnId;
}
