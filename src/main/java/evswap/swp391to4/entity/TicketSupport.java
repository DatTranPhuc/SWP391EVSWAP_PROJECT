package evswap.swp391to4.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "ticket_support")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketSupport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Integer ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    private String category; // station/battery/payment
    @Column(columnDefinition = "text")
    private String comment;

    private String status; // open/in_progress/resolved/closed

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(columnDefinition = "text")
    private String note;
}
