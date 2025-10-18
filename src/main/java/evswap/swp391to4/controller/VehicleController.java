package evswap.swp391to4.controller;

import evswap.swp391to4.dto.VehicleRegistrationForm;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.service.DriverService;
import evswap.swp391to4.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vehicles") // Đường dẫn chung cho tất cả các request trong controller này
public class VehicleController {

    private final DriverService driverService;
    private final VehicleService vehicleService;

    // API này có thể giữ nguyên hoặc thay đổi tùy theo cấu trúc API của bạn
    @PostMapping("/api/drivers/{driverId}/vehicles")
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

    /**
     * Hiển thị form đăng ký phương tiện.
     * URL: GET /vehicles/register
     */
    @GetMapping("/register")
    public String showRegistrationForm(@RequestParam("driverId") Integer driverId,
                                       Model model,
                                       RedirectAttributes redirect) {
        try {
            Driver driver = driverService.getDriverById(driverId);

            model.addAttribute("driverId", driver.getDriverId());
            model.addAttribute("driverName", driver.getFullName());
            model.addAttribute("driverInitial", extractInitial(driver.getFullName()));

            if (!model.containsAttribute("vehicleForm")) {
                model.addAttribute("vehicleForm", new VehicleRegistrationForm());
            }

            return "vehicle-register";
        } catch (Exception e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * Xử lý việc đăng ký phương tiện mới.
     * URL: POST /vehicles/register
     */
    @PostMapping("/register")
    public String registerVehicle(@RequestParam("driverId") Integer driverId,
                                  @ModelAttribute("vehicleForm") VehicleRegistrationForm form,
                                  RedirectAttributes redirect) {
        try {
            Vehicle vehicle = Vehicle.builder()
                    .model(form.getModel())
                    .vin(form.getVin())
                    .plateNumber(form.getPlateNumber())
                    .build();

            vehicleService.addVehicleToDriver(driverId, vehicle);
            redirect.addFlashAttribute("loginSuccess", "Đăng ký phương tiện thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("vehicleError", e.getMessage());
            redirect.addFlashAttribute("vehicleForm", form);
            redirect.addAttribute("driverId", driverId);
            return "redirect:/vehicles/register";
        }
    }

    /**
     * Hiển thị trang quản lý phương tiện cho tài xế đã đăng nhập.
     * URL: GET /vehicles
     */
    @GetMapping
    public String manageVehicles(HttpSession session,
                                 Model model,
                                 RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để quản lý phương tiện");
            return "redirect:/login";
        }

        var vehicleCards = buildVehicleCards(driver.getDriverId());
        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("vehicleCards", vehicleCards);
        model.addAttribute("totalVehicles", vehicleCards.size());
        model.addAttribute("lastUpdatedAt", vehicleCards.stream()
                .map(VehicleCardView::createdAt)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null));

        if (!model.containsAttribute("vehicleForm")) {
            model.addAttribute("vehicleForm", new VehicleRegistrationForm());
        }

        return "vehicle-manage";
    }

    /**
     * Xử lý việc thêm phương tiện mới từ trang quản lý.
     * URL: POST /vehicles
     */
    @PostMapping
    public String addVehicleFromManager(@ModelAttribute("vehicleForm") VehicleRegistrationForm form,
                                        HttpSession session,
                                        RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để quản lý phương tiện");
            return "redirect:/login";
        }

        try {
            Vehicle vehicle = Vehicle.builder()
                    .model(form.getModel())
                    .vin(form.getVin())
                    .plateNumber(form.getPlateNumber())
                    .build();

            vehicleService.addVehicleToDriver(driver.getDriverId(), vehicle);

            Driver refreshed = driverService.getDriverById(driver.getDriverId());
            session.setAttribute("loggedInDriver", refreshed);

            redirect.addFlashAttribute("vehicleSuccess", "Thêm phương tiện mới thành công!");
            return "redirect:/vehicles"; // Chuyển hướng về trang quản lý
        } catch (Exception e) {
            redirect.addFlashAttribute("vehicleError", e.getMessage());
            redirect.addFlashAttribute("vehicleForm", form);
            return "redirect:/vehicles"; // Chuyển hướng về trang quản lý
        }
    }

    // --- CÁC PHƯƠNG THỨC HỖ TRỢ (PRIVATE) ---

    private String extractInitial(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "U";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }

    public record VehicleRequest(String vin, String plateNumber, String model) {
    }

    // Lớp nội bộ để hiển thị dữ liệu trên view, giữ nguyên
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

        // Các phương thức getter giữ nguyên...
        public Integer vehicleId() { return vehicleId; }
        public String vehicleName() { return vehicleName; }
        public String plateNumber() { return plateNumber; }
        public String vin() { return vin; }
        public String model() { return model; }
        public Instant createdAt() { return createdAt; }
        public String statusLabel() { return statusLabel; }
        public String statusBadge() { return statusBadge; }
        public String batteryModel() { return batteryModel; }
        public String batteryStatus() { return batteryStatus; }
        public int batteryPercent() { return batteryPercent; }
        public String healthLabel() { return healthLabel; }
        public String healthDescription() { return healthDescription; }
    }

    // Phương thức build card view, giữ nguyên
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

    // Phương thức đoán model pin, giữ nguyên
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