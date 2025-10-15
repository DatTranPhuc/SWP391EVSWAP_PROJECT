package evswap.swp391to4.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Integer driverId;
    private Integer reservationId;
    private BigDecimal amount;
    private String method;
    private String currency;
    private String providerTxnId;
}
