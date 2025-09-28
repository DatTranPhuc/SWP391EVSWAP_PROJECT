package evswap.swp391to4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "swap_transaction")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SwapTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "swap_id")
    private Integer swapId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battery_out_id")
    private Battery batteryOut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battery_in_id")
    private Battery batteryIn;

    @Column(name = "swapped_at")
    private Instant swappedAt;

    private String result; // success/failed/aborted
}
