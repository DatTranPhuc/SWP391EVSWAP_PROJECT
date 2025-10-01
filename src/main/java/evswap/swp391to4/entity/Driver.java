package evswap.swp391to4.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "driver")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Driver {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // Mã OTP gửi qua Gmail
    @Column(name = "email_otp")
    private String emailOtp;

    // Thời gian hết hạn OTP
    @Column(name = "otp_expiry")
    private Instant otpExpiry;

    @Column(name = "created_at")
    private Instant createdAt;
}
