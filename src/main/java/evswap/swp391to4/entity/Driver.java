package evswap.swp391to4.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "driver")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "vehicles") @EqualsAndHashCode(exclude = "vehicles")
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

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_otp")
    private String emailOtp;

    @Column(name = "otp_expiry")
    private Instant otpExpiry;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vehicle> vehicles = new ArrayList<>();

    @PrePersist
    void prePersist(){
        if (createdAt == null) createdAt = Instant.now();
        if (emailVerified == null) emailVerified = false;
    }
}
