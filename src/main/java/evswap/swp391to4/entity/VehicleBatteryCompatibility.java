package evswap.swp391to4.entity;

import jakarta.persistence.*;
import lombok.*;

// Embedded ID class moved to its own file VehicleBatteryId.java

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
