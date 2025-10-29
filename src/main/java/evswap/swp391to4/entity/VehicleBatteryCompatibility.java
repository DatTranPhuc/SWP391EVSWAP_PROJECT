package evswap.swp391to4.entity;



import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Table(name = "vehicle_battery_compatibility")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleBatteryCompatibility {

    @Embeddable
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class VehicleBatteryId implements Serializable {
        @Column(name = "vehicle_id")
        private Integer vehicleId;
        @Column(name = "battery_id")
        private Integer batteryId;
    }

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
