package evswap.swp391to4.entity;



import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
class VehicleBatteryId implements Serializable {
    @Column(name = "vehicle_id")
    private Integer vehicleId;
    @Column(name = "battery_id")
    private Integer batteryId;
}

@Entity @Table(name = "vehicle_battery_compatibility")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleBatteryCompatibility {

    @EmbeddedId
    private VehicleBatteryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vehicleId")
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("batteryId")
    @JoinColumn(name = "battery_id")
    private Battery battery;
}
