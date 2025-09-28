package evswap.swp391to4.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "vehicle")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Integer vehicleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(nullable = false, unique = true)
    private String vin;

    @Column(name = "plate_number")
    private String plateNumber;

    private String model;

    @Column(name = "created_at")
    private Instant createdAt;
}

