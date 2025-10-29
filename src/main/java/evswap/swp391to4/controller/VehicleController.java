package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ApiResponse;
import evswap.swp391to4.dto.VehicleRegistrationForm;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.service.DriverService;
import evswap.swp391to4.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final DriverService driverService;
    private final VehicleService vehicleService;

    @PostMapping("/drivers/{driverId}")
    public ResponseEntity<ApiResponse<Vehicle>> addVehicle(@PathVariable Integer driverId,
                                                           @RequestBody VehicleRequest request) {
        Vehicle vehicle = Vehicle.builder()
                .vin(request.vin())
                .plateNumber(request.plateNumber())
                .model(request.model())
                .build();
        Vehicle savedVehicle = vehicleService.addVehicleToDriver(driverId, vehicle);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm phương tiện thành công.", savedVehicle));
    }

    @GetMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> showRegistrationForm(@RequestParam("driverId") Integer driverId) {
        Driver driver = driverService.getDriverById(driverId);

        Map<String, Object> data = new HashMap<>();
        data.put("driverId", driver.getDriverId());
        data.put("driverName", driver.getFullName());
        data.put("driverInitial", extractInitial(driver.getFullName()));
        data.put("requiredFields", List.of("model", "vin", "plateNumber"));

        return ResponseEntity.ok(ApiResponse.success("Thông tin đăng ký phương tiện.", data));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerVehicle(@RequestParam("driverId") Integer driverId,
                                                                            @RequestBody VehicleRegistrationForm form) {
        Vehicle vehicle = Vehicle.builder()
                .model(form.getModel())
                .vin(form.getVin())
                .plateNumber(form.getPlateNumber())
                .build();

        vehicleService.addVehicleToDriver(driverId, vehicle);

        Map<String, Object> data = new HashMap<>();
        data.put("next", "/api/auth/login");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký phương tiện thành công! Vui lòng đăng nhập.", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> manageVehicles(HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            Map<String, Object> data = Map.of("next", "/api/auth/login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, Object>>failure("Vui lòng đăng nhập để quản lý phương tiện.").withData(data));
        }

        List<VehicleCardView> vehicleCards = buildVehicleCards(driver.getDriverId());
        Map<String, Object> data = new HashMap<>();
        data.put("driverName", driver.getFullName());
        data.put("driverInitial", extractInitial(driver.getFullName()));
        data.put("vehicleCards", vehicleCards);
        data.put("totalVehicles", vehicleCards.size());
        data.put("lastUpdatedAt", vehicleCards.stream()
                .map(VehicleCardView::getCreatedAt)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null));

        return ResponseEntity.ok(ApiResponse.success("Danh sách phương tiện", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<VehicleCardView>>> addVehicleFromManager(@RequestBody VehicleRegistrationForm form,
                                                                                    HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<List<VehicleCardView>>failure("Vui lòng đăng nhập để quản lý phương tiện."));
        }

        Vehicle vehicle = Vehicle.builder()
                .model(form.getModel())
                .vin(form.getVin())
                .plateNumber(form.getPlateNumber())
                .build();

        vehicleService.addVehicleToDriver(driver.getDriverId(), vehicle);

        Driver refreshed = driverService.getDriverById(driver.getDriverId());
        session.setAttribute("loggedInDriver", refreshed);

        List<VehicleCardView> updatedCards = buildVehicleCards(driver.getDriverId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm phương tiện mới thành công!", updatedCards));
    }

    private String extractInitial(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "U";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }

    public record VehicleRequest(String vin, String plateNumber, String model) {
    }

    @Getter
    @Builder
    private static class VehicleCardView {
        private final Integer vehicleId;
        private final String vehicleName;
        private final String plateNumber;
        private final String vin;
        private final String model;
        private final Instant createdAt;
        private final String statusLabel;
        private final String statusBadge;
        private final String batteryModel;
        private final String batteryStatus;
        private final int batteryPercent;
        private final String healthLabel;
        private final String healthDescription;
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

            cards.add(VehicleCardView.builder()
                    .vehicleId(vehicle.getVehicleId())
                    .vehicleName(Optional.ofNullable(vehicle.getModel())
                            .filter(name -> !name.isBlank())
                            .orElse("Phương tiện " + (index + 1)))
                    .plateNumber(Optional.ofNullable(vehicle.getPlateNumber())
                            .filter(plate -> !plate.isBlank())
                            .orElse("Chưa cập nhật"))
                    .vin(vehicle.getVin())
                    .model(Optional.ofNullable(vehicle.getModel()).orElse("Chưa cập nhật"))
                    .createdAt(vehicle.getCreatedAt())
                    .statusLabel(batteryPercent >= 75 ? "Đang hoạt động" : "Đang kiểm tra")
                    .statusBadge(statusBadge)
                    .batteryModel(guessBatteryModel(vehicle.getModel()))
                    .batteryStatus(batteryPercent >= 75 ? "Đang sử dụng" : "Cần bảo dưỡng")
                    .batteryPercent(batteryPercent)
                    .healthLabel(healthLabel)
                    .healthDescription(healthDescription)
                    .build());
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