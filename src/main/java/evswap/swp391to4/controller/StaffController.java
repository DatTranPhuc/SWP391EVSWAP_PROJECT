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
     * Hàm Helper Kiểm tra Bảo mật: Đảm bảo nhân viên đã đăng nhập và được gán trạm.
     * Ném IllegalStateException nếu kiểm tra thất bại.
     */
    private Staff checkLogin(HttpSession session) { // Đổi tên cho ngắn gọn
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            throw new IllegalStateException("Bạn chưa đăng nhập! Vui lòng đăng nhập với tư cách Nhân viên."); // Đã dịch
        }
        if (staff.getStation() == null) {
            throw new IllegalStateException("Tài khoản Nhân viên của bạn chưa được gán vào trạm nào. Vui lòng liên hệ Admin."); // Đã dịch
        }
        return staff;
    }

    /**
     * Trang Dashboard của Nhân viên (GET /staff/dashboard)
     * Hiển thị tổng quan và thống kê pin cho trạm của nhân viên.
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirect) {
        try {
            Staff staff = checkLogin(session); // Kiểm tra Xác thực/Quyền hạn
            Station station = staff.getStation();

            // Thông tin cơ bản
            model.addAttribute("staffName", staff.getFullName());
            model.addAttribute("stationName", station.getName());
            model.addAttribute("stationAddress", station.getAddress());
            model.addAttribute("stationId", station.getStationId());

            // Thống kê Pin
            model.addAttribute("fullCount", batteryService.countBatteriesByState(station, "full"));
            model.addAttribute("chargingCount", batteryService.countBatteriesByState(station, "charging"));
            model.addAttribute("maintenanceCount", batteryService.countBatteriesByState(station, "maintenance"));
            model.addAttribute("retiredCount", batteryService.countBatteriesByState(station, "retired"));
            model.addAttribute("totalCount", batteryService.getAllBatteriesForStation(station).size()); // Lấy tổng số lượng

            return "staff/dashboard";

        } catch (IllegalStateException authError) { // Bắt lỗi đăng nhập/gán trạm
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) { // Bắt các lỗi khác (vd: lỗi database)
            model.addAttribute("errorMessage", "Không thể tải dữ liệu dashboard: " + e.getMessage()); // Đã dịch
            // Cố gắng vẫn hiển thị layout dashboard cơ bản
            try {
                Staff staff = (Staff) session.getAttribute("loggedInStaff");
                if(staff != null && staff.getStation() != null) {
                    model.addAttribute("staffName", staff.getFullName());
                    model.addAttribute("stationName", staff.getStation().getName());
                }
            } catch (Exception ignored) {} // Bỏ qua lỗi tiềm ẩn ở đây
            return "staff/dashboard"; // Ở lại trang dashboard nhưng hiển thị lỗi
        }
    }

    /**
     * Trang Quản lý Pin (GET /staff/batteries)
     * Hiển thị danh sách pin (có tìm kiếm/lọc) và form 'Thêm Pin'.
     */
    @GetMapping("/batteries")
    public String manageBatteriesPage(
            @RequestParam(name = "searchType", required = false) String searchType,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            HttpSession session, Model model, RedirectAttributes redirect) {

        try {
            Staff staff = checkLogin(session); // Kiểm tra Auth

            // Lấy danh sách pin dựa trên tìm kiếm/lọc
            List<Battery> batteryList = batteryService.searchBatteriesForStation(staff.getStation(), searchType, searchTerm);
            model.addAttribute("batteryList", batteryList);

            // Thêm thông tin cần thiết cho view
            model.addAttribute("stationName", staff.getStation().getName());
            model.addAttribute("currentSearchType", searchType);
            model.addAttribute("currentSearchTerm", searchTerm);

            // Chuẩn bị DTO rỗng cho form 'Thêm Pin' (nếu chưa có do lỗi)
            if (!model.containsAttribute("newBattery")) {
                model.addAttribute("newBattery", new BatteryCreateRequest());
            }

            return "staff/manage-batteries";

        } catch (IllegalStateException authError) { // Bắt lỗi đăng nhập/gán trạm
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) { // Bắt lỗi khác
            redirect.addFlashAttribute("errorMessage", "Lỗi tải trang quản lý pin: " + e.getMessage()); // Đã dịch
            return "redirect:/staff/dashboard"; // Chuyển hướng về dashboard nếu có lỗi khác
        }
    }

    /**
     * Xử lý Thêm Pin Hàng loạt (POST /staff/batteries/add)
     * Xử lý việc gửi form 'Thêm Pin'.
     */
    @PostMapping("/batteries/add")
    public String handleCreateBattery(
            @Valid @ModelAttribute("newBattery") BatteryCreateRequest dto,
            BindingResult bindingResult,
            HttpSession session, Model model, RedirectAttributes redirect) {

        Staff staff;
        try {
            staff = checkLogin(session); // Kiểm tra Auth trước
        } catch (IllegalStateException authError) {
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        }

        // Kiểm tra lỗi validation
        if (bindingResult.hasErrors()) {
            // Nếu có lỗi, trả về TRANG CŨ để hiển thị lỗi và giữ lại dữ liệu form
            return loadPageForError(model, staff, "Thông tin nhập không hợp lệ. Vui lòng kiểm tra lại."); // Đã dịch
        }

        try {
            // Nếu validation thành công, thử tạo pin
            batteryService.createBatteries(dto, staff);
            redirect.addFlashAttribute("successMessage", "Đã thêm thành công " + dto.getQuantity() + " pin (Model: " + dto.getModel() + ")!"); // Đã dịch
            return "redirect:/staff/batteries"; // Chuyển hướng khi thành công (PRG pattern)

        } catch (Exception logicError) {
            // Nếu service layer ném lỗi (vd: trạng thái không hợp lệ)
            // Trả về TRANG CŨ để hiển thị lỗi và giữ lại dữ liệu form
            return loadPageForError(model, staff, logicError.getMessage());
        }
    }

    /**
     * Hàm Helper: Tải lại dữ liệu trang manage-batteries khi có lỗi form.
     * Đảm bảo danh sách pin vẫn được hiển thị cùng thông báo lỗi.
     */
    private String loadPageForError(Model model, Staff staff, String errorMessage) {
        List<Battery> batteryList = batteryService.getAllBatteriesForStation(staff.getStation()); // Tải tất cả pin
        model.addAttribute("batteryList", batteryList);
        model.addAttribute("stationName", staff.getStation().getName());
        model.addAttribute("createError", errorMessage); // Thông báo lỗi cụ thể cho form tạo
        // DTO ('newBattery') với các lỗi của nó sẽ tự động được thêm lại vào model bởi Spring
        return "staff/manage-batteries"; // Trả về tên view
    }

    /**
     * Xử lý Cập nhật Trạng thái Pin (POST /staff/batteries/update)
     * Xử lý cập nhật để đặt trạng thái thành 'charging', 'maintenance', hoặc 'retired'.
     */
    @PostMapping("/batteries/update")
    public String handleUpdateBatteryState(
            @RequestParam("batteryId") Integer batteryId,
            @RequestParam("newState") String newState,
            HttpSession session, RedirectAttributes redirect) {

        try {
            Staff staff = checkLogin(session); // Kiểm tra Auth
            batteryService.updateBatteryState(batteryId, newState, staff);
            redirect.addFlashAttribute("successMessage", "Đã cập nhật trạng thái Pin #" + batteryId + " thành công!"); // Đã dịch

        } catch (IllegalStateException authError) { // Bắt lỗi đăng nhập/gán trạm
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) { // Bắt lỗi logic (trạng thái không hợp lệ, không tìm thấy pin, etc.)
            redirect.addFlashAttribute("errorMessage", "Lỗi cập nhật pin: " + e.getMessage()); // Đã dịch
        }

        return "redirect:/staff/batteries"; // Luôn chuyển hướng về trang quản lý
    }

    /**
     * Xử lý Giả lập Sạc Đầy (POST /staff/batteries/mark-full)
     * Mô phỏng IoT đánh dấu pin 'charging' thành 'full'.
     */
    @PostMapping("/batteries/mark-full")
    public String handleMarkAsFull(
            @RequestParam("batteryId") Integer batteryId,
            HttpSession session, RedirectAttributes redirect) {

        try {
            Staff staff = checkLogin(session); // Kiểm tra Auth
            batteryService.markBatteryAsFull(batteryId, staff);
            redirect.addFlashAttribute("successMessage", "Đã giả lập sạc đầy cho Pin #" + batteryId + "!"); // Đã dịch

        } catch (IllegalStateException authError) { // Bắt lỗi đăng nhập/gán trạm
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) { // Bắt lỗi logic (không phải charging, không tìm thấy, etc.)
            redirect.addFlashAttribute("errorMessage", "Lỗi đánh dấu đầy: " + e.getMessage()); // Đã dịch
        }

        return "redirect:/staff/batteries"; // Luôn chuyển hướng về trang quản lý
    }

    /**
     * Xử lý Giả lập Sử dụng Pin (POST /staff/batteries/simulate-usage)
     * Cho phép nhân viên đặt thủ công phần trăm SOC.
     */
    @PostMapping("/batteries/simulate-usage")
    public String handleSimulateUsage(
            @RequestParam("batteryId") Integer batteryId,
            @RequestParam("newSocPercent") Integer newSocPercent,
            HttpSession session, RedirectAttributes redirect) {

        try {
            Staff staff = checkLogin(session); // Kiểm tra Auth
            batteryService.simulateBatteryUsage(batteryId, newSocPercent, staff);
            redirect.addFlashAttribute("successMessage", "Đã giả lập sử dụng cho Pin #" + batteryId + " (SOC mới: " + newSocPercent + "%)!"); // Đã dịch

        } catch (IllegalStateException authError) { // Bắt lỗi đăng nhập/gán trạm
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) { // Bắt lỗi logic (SOC không hợp lệ, không tìm thấy, etc.)
            redirect.addFlashAttribute("errorMessage", "Lỗi giả lập sử dụng: " + e.getMessage()); // Đã dịch
        }

        return "redirect:/staff/batteries"; // Luôn chuyển hướng về trang quản lý
    }
}