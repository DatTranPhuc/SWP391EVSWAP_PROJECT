package evswap.swp391to4.controller;

import evswap.swp391to4.dto.BatteryCreateRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.service.BatteryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final BatteryService batteryService;

    /**
     * Hàm helper kiểm tra login (Code của bạn đã tốt)
     */
    private Staff checkStaffLogin(HttpSession session) {
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            throw new IllegalStateException("Bạn chưa đăng nhập! Vui lòng đăng nhập với tư cách Staff.");
        }
        if (staff.getStation() == null) {
            throw new IllegalStateException("Tài khoản Staff của bạn chưa được gán vào trạm nào. Vui lòng liên hệ Admin.");
        }
        return staff;
    }

    /**
     * Trang Dashboard (Code của bạn đã tốt)
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            Station station = staff.getStation();

            model.addAttribute("staffName", staff.getFullName());
            model.addAttribute("stationName", station.getName());
            model.addAttribute("stationAddress", station.getAddress());
            model.addAttribute("stationId", station.getStationId());

            // Lấy số liệu thống kê
            model.addAttribute("fullCount", batteryService.countBatteriesByState(station, "full"));
            model.addAttribute("chargingCount", batteryService.countBatteriesByState(station, "charging"));
            model.addAttribute("maintenanceCount", batteryService.countBatteriesByState(station, "maintenance"));
            model.addAttribute("retiredCount", batteryService.countBatteriesByState(station, "retired"));
            model.addAttribute("totalCount", batteryService.getAllBatteriesForStation(station).size()); // Giữ theo yêu cầu

            return "staff/dashboard";

        } catch (IllegalStateException authError) {
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không thể tải dữ liệu dashboard: " + e.getMessage());
            return "staff/dashboard";
        }
    }

    /**
     * Trang Quản lý Pin (Tất cả pin) (Code của bạn đã tốt)
     */
    @GetMapping("/batteries")
    public String manageBatteriesPage(
            @RequestParam(name = "searchType", required = false) String searchType,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {

        try {
            Staff staff = checkStaffLogin(session);

            List<Battery> batteryList = batteryService.searchBatteriesForStation(staff.getStation(), searchType, searchTerm);
            model.addAttribute("batteryList", batteryList);
            model.addAttribute("stationName", staff.getStation().getName());
            model.addAttribute("currentSearchType", searchType);
            model.addAttribute("currentSearchTerm", searchTerm);

            if (!model.containsAttribute("newBattery")) {
                model.addAttribute("newBattery", new BatteryCreateRequest());
            }

            return "staff/manage-batteries";

        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            if (e instanceof IllegalStateException) {
                return "redirect:/login";
            }
            return "redirect:/staff/dashboard";
        }
    }

    // ===============================================
    // ===== HÀM MỚI (ĐỂ HIỂN THỊ TRANG SẠC PIN) =====
    // ===============================================
    /**
     * Hiển thị trang "Trạm Sạc" chuyên dụng.
     * Chỉ liệt kê các pin sẵn sàng để sạc (state = 'maintenance').
     */
    @GetMapping("/charge-station")
    public String chargeStationPage(HttpSession session, Model model, RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            Station station = staff.getStation();

            // 1. Lấy pin CHỜ SẠC (state = 'maintenance')
            List<Battery> chargeableBatteries = batteryService.searchBatteriesForStation(station, "state", "maintenance");

            // 2. (MỚI) Lấy pin ĐANG SẠC (state = 'charging')
            List<Battery> chargingBatteries = batteryService.searchBatteriesForStation(station, "state", "charging");

            model.addAttribute("chargeableBatteries", chargeableBatteries);
            model.addAttribute("chargingBatteries", chargingBatteries); // <-- Thêm dòng này
            model.addAttribute("stationName", station.getName());

            return "staff/charge-station";

        } catch (IllegalStateException authError) {
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/staff/dashboard";
        }
    }
    // ===============================================

    /**
     * Xử lý Thêm Pin Mới (Code của bạn đã tốt)
     */
    @PostMapping("/batteries/add")
    public String handleCreateBattery(
            @Valid @ModelAttribute("newBattery") BatteryCreateRequest dto,
            BindingResult bindingResult,
            HttpSession session, Model model, RedirectAttributes redirect) {

        Staff staff;
        try {
            staff = checkStaffLogin(session);
        } catch (IllegalStateException authError) {
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            // Lỗi validation, load lại trang 'manage-batteries'
            return loadPageForError(model, staff, "Thông tin nhập không hợp lệ. Vui lòng kiểm tra lại.");
        }

        try {
            batteryService.createBatteries(dto, staff);
            redirect.addFlashAttribute("successMessage", "Đã thêm " + dto.getQuantity() + " pin (Model: " + dto.getModel() + ") thành công!");
            return "redirect:/staff/batteries"; // Redirect về trang quản lý chung

        } catch (Exception logicError) {
            return loadPageForError(model, staff, logicError.getMessage());
        }
    }

    /**
     * HÀM HELPER (Code của bạn đã tốt)
     * Dùng khi thêm pin bị lỗi validation
     */
    private String loadPageForError(Model model, Staff staff, String errorMessage) {
        List<Battery> batteryList = batteryService.getAllBatteriesForStation(staff.getStation());
        model.addAttribute("batteryList", batteryList);
        model.addAttribute("stationName", staff.getStation().getName());
        model.addAttribute("createError", errorMessage);
        return "staff/manage-batteries"; // Trả về trang quản lý
    }

    /**
     * Xử lý Cập nhật Trạng thái thủ công (Maintenance, Retired)
     * (Code của bạn đã tốt)
     */
    @PostMapping("/batteries/update")
    public String handleUpdateBatteryState(
            @RequestParam("batteryId") Integer batteryId,
            @RequestParam("newState") String newState,
            HttpSession session, RedirectAttributes redirect) {

        try {
            Staff staff = checkStaffLogin(session);
            batteryService.updateBatteryState(batteryId, newState, staff);
            redirect.addFlashAttribute("successMessage", "Đã cập nhật Pin #" + batteryId + " thành công!");

        } catch (IllegalStateException authError) {
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/staff/batteries"; // Redirect về trang quản lý chung
    }


    /**
     * Xử lý Bắt đầu Sạc Pin (cho nút "Sạc Pin")
     * (SỬA LẠI REDIRECT)
     */
    @PostMapping("/batteries/start-charge")
    public String handleStartCharging(
            @RequestParam("batteryId") Integer batteryId,
            HttpSession session,
            RedirectAttributes redirect) {

        try {
            Staff staff = checkStaffLogin(session);
            batteryService.startChargingBattery(batteryId, staff);
            redirect.addFlashAttribute("successMessage", "Đã bắt đầu sạc cho Pin #" + batteryId + ".");

        } catch (IllegalStateException authError) {
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        // ===== SỬA LẠI REDIRECT =====
        // Redirect về trang "Trạm Sạc" nơi staff vừa bấm nút
        return "redirect:/staff/charge-station";
    }
}