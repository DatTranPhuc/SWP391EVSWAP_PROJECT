package evswap.swp391to4.entity;


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
    @Column(columnDefinition = "nvarchar(MAX)")
    private String comment;

    private String status; // open/in_progress/resolved/closed

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(columnDefinition = "nvarchar(MAX)")
    private String note;

    @Column(name = "comment_history", columnDefinition = "nvarchar(MAX)")
    private String commentHistory;

    @Column(columnDefinition = "nvarchar(MAX)")
    private String attachments;
}
