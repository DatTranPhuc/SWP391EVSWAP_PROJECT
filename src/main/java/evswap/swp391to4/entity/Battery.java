package evswap.swp391to4.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "battery")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Battery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "battery_id")
    private Integer batteryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false)
    private String model;

    private String state;     // full/charging/maintenance/retired
    @Column(name = "soh_percent")
    private Integer sohPercent;
    @Column(name = "soc_percent")
    private Integer socPercent;

    @Column(name = "reserved_for_reservation_id")
    private Integer reservedForReservationId;

    @Column(name = "reserved_at")
    private Instant reservedAt;
}
