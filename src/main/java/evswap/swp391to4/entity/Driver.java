package evswap.swp391to4.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal; // <-- Thêm import này
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "driver")
@Data // <-- Annotation này sẽ tự tạo getBalance() và setBalance()
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"vehicles", "payments"}) // Thêm payments vào exclude nếu có
@EqualsAndHashCode(exclude = {"vehicles", "payments"}) // Thêm payments vào exclude nếu có
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Integer driverId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true)
    private String phone;

    // --- [THÊM TRƯỜNG BALANCE VÀO ĐÂY] ---
    @Column(precision = 19, scale = 2, nullable = false, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
    private BigDecimal balance;
    // ------------------------------------

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "email_otp")
    private String emailOtp;

    @Column(name = "otp_expiry")
    private Instant otpExpiry;

    @Column(name = "created_at")
    private Instant createdAt;

    // --- Mối quan hệ OneToMany ---
    @Builder.Default
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vehicle> vehicles = new ArrayList<>();

    // Thêm mối quan hệ với Payment (nếu chưa có)
    @OneToMany(mappedBy = "driver")
    private List<Payment> payments;
}