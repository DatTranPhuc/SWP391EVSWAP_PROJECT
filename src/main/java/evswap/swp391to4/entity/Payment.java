package evswap.swp391to4.entity;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

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

    private String currency;
    @Column(name = "provider_txn_id")
    private String providerTxnId;
}
