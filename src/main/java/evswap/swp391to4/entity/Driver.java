package evswap.swp391to4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal; // <-- Thêm import cho BigDecimal
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "driver")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "vehicles")
@EqualsAndHashCode(exclude = "vehicles")
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

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "email_otp")
    private String emailOtp;

    @Column(name = "otp_expiry")
    private Instant otpExpiry;

    // [ĐÃ THÊM] Trường để lưu số dư tài khoản
    @Column(precision = 19, scale = 2, nullable = false, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
    private BigDecimal balance;

    @Column(name = "created_at")
    private Instant createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vehicle> vehicles = new ArrayList<>();

    // Bạn có thể thêm các mối quan hệ khác ở đây nếu cần, ví dụ với Payment
    @OneToMany(mappedBy = "driver")
    private List<Payment> payments;
}