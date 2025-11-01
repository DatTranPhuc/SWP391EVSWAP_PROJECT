package evswap.swp391to4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Integer reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    // CHO PHÉP NULL TẠM THỜI để thêm cột và backfill dữ liệu
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "vehicle_id", nullable = true)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_battery_id")
    private Battery assignedBattery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_battery_id")
    private Battery proposedBattery;

    @Column(name = "price_amount", precision = 12, scale = 2)
    private BigDecimal priceAmount;

    @Column(name = "reserved_start")
    private Instant reservedStart;

    // pending / confirmed / canceled / no_show / completed / checked_in
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;

    // ===== QR fields =====
    @Column(name = "qr_nonce")
    private String qrNonce;

    @Column(name = "qr_expires_at")
    private Instant qrExpiresAt;

    @Column(name = "qr_status")
    private String qrStatus; // active / expired / revoked / used

    @Column(name = "qr_token")
    private String qrToken;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;
}
