package evswap.swp391to4.entity;



import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Table(name = "payment")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation; // nullable

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private String method; // cash/card/ewallet
    private String status; // pending/succeed/failed/refunded

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(columnDefinition = "NVARCHAR(10)")
    private String currency;

    @Column(name = "provider_txn_id", columnDefinition = "nvarchar(MAX)")
    private String providerTxnId;
    
    @Column(name = "order_code")
    private String orderCode; // PayOS orderCode for lookup
    
    @Column(name = "checkout_url", columnDefinition = "nvarchar(MAX)")
    private String checkoutUrl; // PayOS checkout URL
}
