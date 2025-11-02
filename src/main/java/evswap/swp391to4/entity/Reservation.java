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

@Entity @Table(name = "reservation")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Integer reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_battery_id")
    private Battery assignedBattery;

    @Column(name = "price_amount", precision = 12, scale = 2)
    private BigDecimal priceAmount;

    @Column(name = "reserved_start")
    private Instant reservedStart;

    private String status; // pending/confirmed/canceled/no_show/completed

    @Column(name = "created_at")
    private Instant createdAt;

    // QR fields
    @Column(name = "qr_nonce")
    private String qrNonce;

    @Column(name = "qr_expires_at")
    private Instant qrExpiresAt;

    @Column(name = "qr_status")
    private String qrStatus; // active/expired/revoked/used

    @Column(name = "qr_token")
    private String qrToken;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    // Payment fields
    @Column(name = "payment_method")
    private String paymentMethod; // "wallet", "cash", "transfer"

    @Column(name = "payment_status")
    private String paymentStatus; // "pending", "completed", "failed"

    @Column(name = "is_instant_swap")
    private Boolean isInstantSwap; // true for instant swap, false for scheduled
}

