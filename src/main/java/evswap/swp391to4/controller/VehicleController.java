package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/drivers/{driverId}/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<Vehicle> addVehicle(@PathVariable Integer driverId,
                                              @RequestBody VehicleRequest request) {
        Vehicle vehicle = Vehicle.builder()
                .vin(request.vin())
                .plateNumber(request.plateNumber())
                .model(request.model())
                .build();
        Vehicle savedVehicle = vehicleService.addVehicleToDriver(driverId, vehicle);
        return ResponseEntity.ok(savedVehicle);
    }

    public record VehicleRequest(String vin, String plateNumber, String model) {
    }
}
