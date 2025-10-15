package evswap.swp391to4.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.VehicleRepository;
import evswap.swp391to4.service.VehicleDetailService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final VehicleDetailService vehicleDetailService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getVehicles(@RequestHeader("Authorization") String authHeader) {
        try {
            List<Vehicle> vehicles = vehicleRepository.findAll();
            List<Map<String, Object>> vehicleList = vehicles.stream()
                .map(vehicle -> {
                    Map<String, Object> vehicleData = new HashMap<>();
                    vehicleData.put("vehicleId", vehicle.getVehicleId());
                    vehicleData.put("vin", vehicle.getVin());
                    vehicleData.put("plateNumber", vehicle.getPlateNumber());
                    vehicleData.put("model", vehicle.getModel());
                    vehicleData.put("createdAt", vehicle.getCreatedAt());
                    if (vehicle.getDriver() != null) {
                        vehicleData.put("driverId", vehicle.getDriver().getDriverId());
                        vehicleData.put("driverName", vehicle.getDriver().getFullName());
                    }
                    return vehicleData;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("vehicles", vehicleList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addVehicle(@RequestHeader("Authorization") String authHeader,
                                                          @RequestBody Map<String, Object> request) {
        try {
            String vin = (String) request.get("vin");
            String plateNumber = (String) request.get("plateNumber");
            String model = (String) request.get("model");
            Integer driverId = (Integer) request.get("driverId");

            if (driverId == null) {
                driverId = 1; // Default driver for demo
            }

            Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

            Vehicle vehicle = Vehicle.builder()
                .vin(vin)
                .plateNumber(plateNumber)
                .model(model)
                .driver(driver)
                .createdAt(Instant.now())
                .build();

            Vehicle savedVehicle = vehicleRepository.save(vehicle);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vehicle", Map.of(
                "vehicleId", savedVehicle.getVehicleId(),
                "vin", savedVehicle.getVin(),
                "plateNumber", savedVehicle.getPlateNumber(),
                "model", savedVehicle.getModel()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<Map<String, Object>> getVehicle(@PathVariable Integer vehicleId,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

            Map<String, Object> vehicleData = new HashMap<>();
            vehicleData.put("vehicleId", vehicle.getVehicleId());
            vehicleData.put("vin", vehicle.getVin());
            vehicleData.put("plateNumber", vehicle.getPlateNumber());
            vehicleData.put("model", vehicle.getModel());
            vehicleData.put("createdAt", vehicle.getCreatedAt());
            
            if (vehicle.getDriver() != null) {
                vehicleData.put("driverId", vehicle.getDriver().getDriverId());
                vehicleData.put("driverName", vehicle.getDriver().getFullName());
            }

            return ResponseEntity.ok(vehicleData);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<Map<String, Object>> updateVehicle(@PathVariable Integer vehicleId,
                                                             @RequestHeader("Authorization") String authHeader,
                                                             @RequestBody Map<String, Object> request) {
        try {
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

            String vin = (String) request.get("vin");
            String plateNumber = (String) request.get("plateNumber");
            String model = (String) request.get("model");

            if (vin != null) vehicle.setVin(vin);
            if (plateNumber != null) vehicle.setPlateNumber(plateNumber);
            if (model != null) vehicle.setModel(model);

            Vehicle savedVehicle = vehicleRepository.save(vehicle);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vehicle", Map.of(
                "vehicleId", savedVehicle.getVehicleId(),
                "vin", savedVehicle.getVin(),
                "plateNumber", savedVehicle.getPlateNumber(),
                "model", savedVehicle.getModel()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Map<String, Object>> deleteVehicle(@PathVariable Integer vehicleId,
                                                             @RequestHeader("Authorization") String authHeader) {
        try {
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

            vehicleRepository.delete(vehicle);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vehicle deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy thông tin chi tiết xe và pin của driver
     */
    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> getVehicleDetail(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer driverId = 1; // In production, extract from JWT token
            
            Map<String, Object> vehicleDetail = vehicleDetailService.getVehicleDetail(driverId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vehicleDetail", vehicleDetail);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}