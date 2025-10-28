package evswap.swp391to4.entity;



import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "vehicle_battery_compatibility")
@Data @NoArgsConstructor @AllArgsConstructor
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
