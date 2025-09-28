package evswap.swp391to4.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "notification")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_id")
    private Integer notiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    private String type;
    @Column(nullable = false)
    private String title;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "sent_at")
    private Instant sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation; // nullable

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment; // nullable
}
