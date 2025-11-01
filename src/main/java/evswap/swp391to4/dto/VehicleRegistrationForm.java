package evswap.swp391to4.dto;

import evswap.swp391to4.entity.VehicleType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleRegistrationForm {
    private String model;
    private String vin;
    private String plateNumber;
    @NotNull
    private VehicleType vehicleType = VehicleType.TWO_WHEEL;
}
