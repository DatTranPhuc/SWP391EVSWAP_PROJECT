package evswap.swp391to4.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleRepository vehicleRepo;
    private final DriverRepository driverRepo;

    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody CreateVehicleRequest req) {
        try {
            Driver driver = driverRepo.findByEmail(req.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Driver không tồn tại"));

            if (vehicleRepo.existsByVin(req.getVin())) {
                throw new IllegalStateException("VIN đã tồn tại");
            }

            Vehicle vehicle = Vehicle.builder()
                    .driver(driver)
                    .vin(req.getVin())
                    .plateNumber(req.getLicensePlate())
                    .model(req.getModel())
                    .createdAt(Instant.now())
                    .build();

            vehicleRepo.save(vehicle);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("vehicleId", vehicle.getVehicleId());
            return ResponseEntity.status(201).body(resp);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Server error: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @Data
    public static class CreateVehicleRequest {
        private String email; // liên kết driver qua email đăng ký
        private String model;
        private String vin;
        private String licensePlate;
    }
}


