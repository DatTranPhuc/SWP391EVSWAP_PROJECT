package evswap.swp391to4.controller;

import evswap.swp391to4.dto.VehicleRegistrationForm;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.service.DriverService;
import evswap.swp391to4.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleRegistrationController {

    private final DriverService driverService;
    private final VehicleService vehicleService;

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
}
