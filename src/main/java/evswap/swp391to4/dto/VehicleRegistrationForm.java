package evswap.swp391to4.dto;

import evswap.swp391to4.entity.VehicleType;
import lombok.Data;

@Data
public class VehicleRegistrationForm {
    private String model;
    private String vin;
    private String plateNumber;
    private String vehicleType = VehicleType.CITY_48V.name();
}
