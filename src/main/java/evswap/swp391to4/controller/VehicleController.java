package evswap.swp391to4.controller;

import evswap.swp391to4.dto.VehicleRegistrationForm;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.service.DriverService;
import evswap.swp391to4.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final DriverService driverService;
    private final VehicleService vehicleService;

    /**
     * Đăng ký phương tiện mới cho tài xế
     * POST /api/vehicles/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerVehicle(@RequestHeader("Driver-Id") Integer driverId,
                                             @Validated @RequestBody VehicleRegistrationForm form) {
        try {
            Vehicle vehicle = Vehicle.builder()
                    .model(form.getModel())
                    .vin(form.getVin())
                    .plateNumber(form.getPlateNumber())
                    .build();

            Vehicle saved = vehicleService.addVehicleToDriver(driverId, vehicle);
            return ResponseEntity.ok(Map.of(
                    "message", "Đăng ký phương tiện thành công!",
                    "vehicleId", saved.getVehicleId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách phương tiện của tài xế hiện tại
     * GET /api/vehicles
     */
    @GetMapping
    public ResponseEntity<List<Vehicle>> getVehicles(@RequestHeader("Driver-Id") Integer driverId) {
        if (driverId == null) {
            return ResponseEntity.status(401).body(null);
        }
        List<Vehicle> vehicles = vehicleService.getVehiclesForDriver(driverId);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Thêm phương tiện mới (tương tự đăng ký, route dùng cho quản lý)
     * POST /api/vehicles/add
     */
    @PostMapping("/add")
    public ResponseEntity<?> addVehicle(@RequestHeader("Driver-Id") Integer driverId,
                                        @Validated @RequestBody VehicleRegistrationForm form) {
        try {
            Vehicle vehicle = Vehicle.builder()
                    .model(form.getModel())
                    .vin(form.getVin())
                    .plateNumber(form.getPlateNumber())
                    .build();

            Vehicle saved = vehicleService.addVehicleToDriver(driverId, vehicle);
            return ResponseEntity.ok(Map.of(
                    "message", "Thêm phương tiện mới thành công!",
                    "vehicleId", saved.getVehicleId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
