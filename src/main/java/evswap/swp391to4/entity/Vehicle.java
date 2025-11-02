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
import lombok.ToString;

@Entity @Table(name = "vehicle")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "driver")
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

    @Column(name = "vehicle_type", columnDefinition = "NVARCHAR(50)")
    private String vehicleType; // "motorcycle" or "car"

    @Column(name = "created_at")
    private Instant createdAt;
}

