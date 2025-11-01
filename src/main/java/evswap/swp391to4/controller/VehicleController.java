package evswap.swp391to4.controller;

import evswap.swp391to4.dto.VehicleRegistrationForm;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.entity.VehicleType;
import evswap.swp391to4.service.DriverService;
import evswap.swp391to4.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vehicles")
public class VehicleController {

    private final DriverService driverService;
    private final VehicleService vehicleService;

    @ModelAttribute("vehicleTypes")
    public VehicleType[] vehicleTypes() {
        return VehicleType.values();
    }

    @GetMapping("/register")
    public String showRegistrationForm(@RequestParam("driverId") Integer driverId,
                                       Model model,
                                       RedirectAttributes redirect) {
        try {
            Driver driver = driverService.getDriverById(driverId);
            model.addAttribute("driver", driver);
            model.addAttribute("driverId", driver.getDriverId());
            if (!model.containsAttribute("vehicleForm")) {
                model.addAttribute("vehicleForm", new VehicleRegistrationForm());
            }
            return "vehicle-register";
        } catch (Exception e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/register")
    public String registerVehicle(@RequestParam("driverId") Integer driverId,
                                  @ModelAttribute("vehicleForm") VehicleRegistrationForm form,
                                  RedirectAttributes redirect) {
        try {
            vehicleService.addVehicleToDriver(driverId, createVehicle(form));
            redirect.addFlashAttribute("loginSuccess", "Đăng ký phương tiện thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("vehicleError", e.getMessage());
            redirect.addFlashAttribute("vehicleForm", form);
            redirect.addAttribute("driverId", driverId);
            return "redirect:/vehicles/register";
        }
    }

    @GetMapping
    public String manageVehicles(HttpSession session,
                                 Model model,
                                 RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để quản lý phương tiện");
            return "redirect:/login";
        }

        List<Vehicle> vehicles = vehicleService.getVehiclesForDriver(driver.getDriverId());
        model.addAttribute("driver", driver);
        model.addAttribute("vehicles", vehicles);
        if (!model.containsAttribute("vehicleForm")) {
            model.addAttribute("vehicleForm", new VehicleRegistrationForm());
        }

        return "vehicle-manage";
    }

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
            vehicleService.addVehicleToDriver(driver.getDriverId(), createVehicle(form));
            Driver refreshed = driverService.getDriverById(driver.getDriverId());
            session.setAttribute("loggedInDriver", refreshed);
            redirect.addFlashAttribute("vehicleSuccess", "Thêm phương tiện mới thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("vehicleError", e.getMessage());
            redirect.addFlashAttribute("vehicleForm", form);
        }

        return "redirect:/vehicles";
    }

    @PostMapping("/api/drivers/{driverId}/vehicles")
    public ResponseEntity<Vehicle> addVehicle(@PathVariable Integer driverId,
                                              @RequestBody VehicleRequest request) {
        Vehicle vehicle = Vehicle.builder()
                .vin(request.vin())
                .plateNumber(request.plateNumber())
                .model(request.model())
                .vehicleType(resolveVehicleType(request.vehicleType()))
                .build();
        Vehicle savedVehicle = vehicleService.addVehicleToDriver(driverId, vehicle);
        return ResponseEntity.ok(savedVehicle);
    }

    private Vehicle createVehicle(VehicleRegistrationForm form) {
        VehicleType vehicleType = resolveVehicleType(form.getVehicleType());
        return Vehicle.builder()
                .model(form.getModel())
                .vin(form.getVin())
                .plateNumber(form.getPlateNumber())
                .vehicleType(vehicleType)
                .build();
    }

    private VehicleType resolveVehicleType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn phân loại xe phù hợp");
        }
        return VehicleType.fromString(rawType);
    }

    public record VehicleRequest(String vin, String plateNumber, String model, String vehicleType) {
    }
}
