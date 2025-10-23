package evswap.swp391to4.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "notification",
        indexes = {
                @Index(name = "idx_noti_driver_time", columnList = "driver_id,sent_at"),
                @Index(name = "idx_noti_unread",      columnList = "driver_id,is_read")
        })
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_id")
    private Integer notiId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    // Loại thông báo: chuẩn hóa để filter dễ
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 40, nullable = false)
    private Category category; // RESERVATION_* / PAYMENT_* / SECURITY_* / NOTICE

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "content", nullable = false, length = 500)
    private String content; // mô tả ngắn để hiện ở dropdown/list

    @Column(name = "link_url", length = 255)
    private String linkUrl;  // click để tới chi tiết

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10, nullable = false)
    @Builder.Default
    private Priority priority = Priority.NORMAL; // NORMAL/HIGH

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation; // optional

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "payment_id")
    private Payment payment; // optional

    @PrePersist
    void prePersist() {
        if (sentAt == null) sentAt = Instant.now();
        if (isRead == null) isRead = false;
        if (priority == null) priority = Priority.NORMAL;
        if (category == null) category = Category.NOTICE;
    }

    public enum Category {
        RESERVATION_CREATED, RESERVATION_REMINDER, QUEUE_READY,
        PAYMENT_SUCCEEDED, PAYMENT_FAILED,
        SECURITY_LOGIN_ALERT,
        NOTICE
    }

    public enum Priority { NORMAL, HIGH }
}
