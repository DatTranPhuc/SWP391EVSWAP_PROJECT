package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.service.DriverService;
import evswap.swp391to4.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VehicleController {

    private final DriverService driverService;
    private final VehicleService vehicleService;

    @PostMapping("/drivers/{driverId}/vehicles")
    public ResponseEntity<?> addVehicle(@PathVariable Integer driverId,
                                        @RequestBody VehicleRequest request) {
        try {
            Vehicle vehicle = Vehicle.builder()
                    .vin(request.vin())
                    .plateNumber(request.plateNumber())
                    .model(request.model())
                    .build();
            Vehicle savedVehicle = vehicleService.addVehicleToDriver(driverId, vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/drivers/{driverId}/vehicles")
    public ResponseEntity<?> getVehicles(@PathVariable Integer driverId) {
        try {
            return ResponseEntity.ok(vehicleService.getVehiclesForDriver(driverId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/drivers/{driverId}/vehicles/overview")
    public ResponseEntity<?> getVehicleOverview(@PathVariable Integer driverId) {
        try {
            Driver driver = driverService.getDriverById(driverId);
            List<VehicleCardView> vehicleCards = buildVehicleCards(driverId);

            Map<String, Object> response = new HashMap<>();
            response.put("driverId", driver.getDriverId());
            response.put("driverName", driver.getFullName());
            response.put("driverInitial", extractInitial(driver.getFullName()));
            response.put("vehicleCards", vehicleCards);
            response.put("totalVehicles", vehicleCards.size());
            response.put("lastUpdatedAt", vehicleCards.stream()
                    .map(VehicleCardView::createdAt)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractInitial(String fullName) {
        if (fullName == null) {
            return "U";
        }
        String trimmed = fullName.trim();
        if (trimmed.isEmpty()) {
            return "U";
        }
        return trimmed.substring(0, 1).toUpperCase();
    }

    public record VehicleRequest(String vin, String plateNumber, String model) {
    }

    public record VehicleCardView(
            Integer vehicleId,
            String vehicleName,
            String plateNumber,
            String vin,
            String model,
            Instant createdAt,
            String statusLabel,
            String statusBadge,
            String batteryModel,
            String batteryStatus,
            int batteryPercent,
            String healthLabel,
            String healthDescription
    ) {
    }

    private List<VehicleCardView> buildVehicleCards(Integer driverId) {
        List<Vehicle> vehicles = vehicleService.getVehiclesForDriver(driverId);
        List<VehicleCardView> cards = new ArrayList<>();

        for (int index = 0; index < vehicles.size(); index++) {
            Vehicle vehicle = vehicles.get(index);
            int batteryPercent = Math.max(68, 98 - (index * 6));

            String healthLabel;
            String healthDescription;
            if (batteryPercent >= 92) {
                healthLabel = "Tình trạng tuyệt vời";
                healthDescription = "Pin hoạt động tối ưu";
            } else if (batteryPercent >= 82) {
                healthLabel = "Hiệu suất ổn định";
                healthDescription = "Sẵn sàng cho hành trình dài";
            } else {
                healthLabel = "Cần theo dõi";
                healthDescription = "Nên kiểm tra pin sớm";
            }

            String statusBadge = batteryPercent >= 75 ? "status-online" : "status-warning";

            cards.add(new VehicleCardView(
                    vehicle.getVehicleId(),
                    Optional.ofNullable(vehicle.getModel())
                            .filter(name -> !name.isBlank())
                            .orElse("Phương tiện " + (index + 1)),
                    Optional.ofNullable(vehicle.getPlateNumber())
                            .filter(plate -> !plate.isBlank())
                            .orElse("Chưa cập nhật"),
                    vehicle.getVin(),
                    Optional.ofNullable(vehicle.getModel()).orElse("Chưa cập nhật"),
                    vehicle.getCreatedAt(),
                    batteryPercent >= 75 ? "Đang hoạt động" : "Đang kiểm tra",
                    statusBadge,
                    guessBatteryModel(vehicle.getModel()),
                    batteryPercent >= 75 ? "Đang sử dụng" : "Cần bảo dưỡng",
                    batteryPercent,
                    healthLabel,
                    healthDescription
            ));
        }

        return cards;
    }

    private String guessBatteryModel(String vehicleModel) {
        if (vehicleModel == null) {
            return "EVS Pack 48V";
        }

        String normalized = vehicleModel.toLowerCase();
        if (normalized.contains("vinfast")) {
            return "VinFast 48V - 20Ah";
        }
        if (normalized.contains("dat")) {
            return "Dat Bike Hypercore";
        }
        return "EVS Pack 48V";
    }
}
