package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.entity.Staff;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OperationsConsoleController {

    @GetMapping("/operations/console")
    public String showOperationsConsole(HttpSession session,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (admin == null && staff == null) {
            redirectAttributes.addFlashAttribute("loginRequired",
                    "Chỉ nhân viên hoặc quản trị viên mới được truy cập bảng điều khiển vận hành.");
            return "redirect:/login";
        }

        String displayName = admin != null
                ? (admin.getFullName() != null ? admin.getFullName() : "Quản trị viên EV")
                : (staff.getFullName() != null ? staff.getFullName() : "Nhân viên EV");
        String trimmed = displayName.trim();
        String initials = trimmed.isEmpty() ? "E" : trimmed.substring(0, 1).toUpperCase();

        model.addAttribute("driverName", displayName);
        model.addAttribute("driverInitial", initials);
        return "operations-console";
    }
}
