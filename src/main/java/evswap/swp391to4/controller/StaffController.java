package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Staff;
// import evswap.swp391to4.service.BatteryService; // Bạn sẽ cần Service này để lấy số liệu pin
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller DÀNH CHO STAFF 👨‍🔧
 * Hiển thị các trang giao diện (dashboard, quản lý pin...)
 * Yêu cầu: Staff phải đăng nhập (được check bằng hàm checkStaffLogin).
 */
@Controller
@RequestMapping("/staff") // Tất cả URL sẽ bắt đầu bằng /staff
@RequiredArgsConstructor
public class StaffController {

    // Ví dụ: Bạn sẽ cần tiêm (Inject) Service để lấy số liệu pin
    // private final BatteryService batteryService;

    /**
     * HÀM HELPER (NỘI BỘ) - "Người Gác Cửa" 💂‍♂️
     * Kiểm tra xem Staff đã đăng nhập hay chưa.
     * Sẽ được gọi ở đầu MỌI hàm trong Controller này.
     */
    private Staff checkStaffLogin(HttpSession session) {
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            // Ném lỗi nếu chưa đăng nhập, lỗi này sẽ được "bắt" ở bên dưới
            throw new IllegalStateException("Bạn chưa đăng nhập! Vui lòng đăng nhập với tư cách Staff.");
        }
        return staff;
    }

    /**
     * Trang Dashboard chính của Staff (Tổng quan)
     * URL: GET /staff/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirect) { // Đổi tên thành "dashboard"
        try {
            // 1. Kiểm tra "vé" (đã đăng nhập chưa)
            Staff staff = checkStaffLogin(session);

            // 2. Lấy thông tin cần thiết và gửi sang file HTML
            model.addAttribute("staffName", staff.getFullName());
            model.addAttribute("stationName", staff.getStation().getName());
            model.addAttribute("stationAddress", staff.getStation().getAddress());
            model.addAttribute("stationId", staff.getStation().getStationId());
            return "staff/dashboard";

        } catch (IllegalStateException e) {
            // 5. Nếu "vé" không hợp lệ (chưa đăng nhập) -> "đá" về trang login
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        }
    }
}