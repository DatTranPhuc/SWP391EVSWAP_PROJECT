package evswap.swp391to4.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleBatteryId implements Serializable {
    @Column(name = "vehicle_id")
    private Integer vehicleId;

    @Column(name = "battery_id")
    private Integer batteryId;
}


