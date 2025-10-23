package evswap.swp391to4.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "reservation",
        indexes = {
                @Index(name = "idx_res_driver_time",  columnList = "driver_id,reserved_start"),
                @Index(name = "idx_res_station_time", columnList = "station_id,reserved_start"),
                @Index(name = "idx_res_status",       columnList = "status")
        })
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Integer reservationId;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "reserved_start", nullable = false)
    private Instant reservedStart;

    @Column(name = "status", length = 20, nullable = false)
    private String status; // pending/confirmed/canceled/no_show/completed

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // QR fields
    @Column(name = "qr_nonce", length = 64)
    private String qrNonce;

    @Column(name = "qr_expires_at")
    private Instant qrExpiresAt;

    @Column(name = "qr_status", length = 20)
    private String qrStatus; // active/expired/revoked/used

    @Column(name = "qr_token", length = 128 /* có thể unique */)
    private String qrToken;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @PrePersist
    void prePersist(){
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = "pending";
    }
}
