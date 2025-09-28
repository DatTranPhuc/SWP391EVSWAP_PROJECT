package evswap.swp391to4.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

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
}

