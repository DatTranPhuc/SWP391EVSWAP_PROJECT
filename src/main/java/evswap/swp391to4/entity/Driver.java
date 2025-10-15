package evswap.swp391to4.entity;



import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity @Table(name = "driver")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "vehicles")
@EqualsAndHashCode(exclude = "vehicles")
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

    @Builder.Default
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vehicle> vehicles = new ArrayList<>();

    // Explicit getters used by controllers/serializers
    public Integer getDriverId() { return this.driverId; }
    public String getFullName() { return this.fullName; }
    public Boolean getEmailVerified() { return this.emailVerified; }
}
