package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.VehicleRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AccountController {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    // 🟢 Hiển thị trang tài khoản
    @Transactional
    @GetMapping("/account")
    public String showAccountPage(
            Model model,
            HttpSession session,
            @RequestParam(value = "id", required = false) Integer driverId
    ) {
        Driver driver = null;

        // Nếu admin truyền id (xem chi tiết tài xế)
        if (driverId != null) {
            driver = driverRepository.findById(driverId).orElse(null);
            if (driver == null) {
                model.addAttribute("error", "Không tìm thấy tài xế với ID = " + driverId);
                return "account";
            }
        } else {
            // Nếu không có id -> tài xế đang tự xem
            driver = (Driver) session.getAttribute("loggedInDriver");
            if (driver == null) {
                return "redirect:/login";
            }
            driver = driverRepository.findById(driver.getDriverId()).orElse(driver);
        }

        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverEmail", driver.getEmail());
        model.addAttribute("driverPhone", driver.getPhone());
        model.addAttribute("driverCreatedAt", driver.getCreatedAt());
        model.addAttribute("vehicles", driver.getVehicles());

        return "account";
    }

    // 🟢 Cập nhật thông tin khách hàng
    @PostMapping("/account/update")
    public String updateAccount(
            @RequestParam String fullName,
            @RequestParam String phone,
            HttpSession session,
            RedirectAttributes ra
    ) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) return "redirect:/login";

        Optional<Driver> optionalDriver = driverRepository.findById(driver.getDriverId());
        if (optionalDriver.isPresent()) {
            Driver existing = optionalDriver.get();
            existing.setFullName(fullName);
            existing.setPhone(phone);
            driverRepository.save(existing);

            session.setAttribute("loggedInDriver", existing);
            ra.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } else {
            ra.addFlashAttribute("error", "Không tìm thấy tài khoản!");
        }

        return "redirect:/account";
    }

    // 🟢 Xóa xe
    @GetMapping("/account/delete-vehicle/{id}")
    public String deleteVehicle(
            @PathVariable("id") Integer vehicleId,
            HttpSession session,
            RedirectAttributes ra
    ) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) return "redirect:/login";

        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Xe không tồn tại!");
            return "redirect:/account";
        }

        Vehicle vehicle = vehicleOpt.get();
        if (!vehicle.getDriver().getDriverId().equals(driver.getDriverId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền xóa xe này!");
            return "redirect:/account";
        }

        vehicleRepository.delete(vehicle);
        ra.addFlashAttribute("success", "Đã xóa xe thành công!");
        return "redirect:/account";
    }

    // 🟢 Xóa tài khoản
    @GetMapping("/account/delete")
    public String deleteAccount(HttpSession session, RedirectAttributes ra) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) return "redirect:/login";

        try {
            driverRepository.deleteById(driver.getDriverId());
            session.invalidate();
            ra.addFlashAttribute("success", "Tài khoản của bạn đã được xóa vĩnh viễn!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa tài khoản: " + e.getMessage());
        }

        return "redirect:/login";
    }
}
