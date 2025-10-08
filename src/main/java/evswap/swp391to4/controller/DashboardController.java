package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Driver;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(HttpSession session, Model model) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver != null) {
            model.addAttribute("driverName", driver.getFullName());
            model.addAttribute("loggedIn", true);
        } else {
            model.addAttribute("loggedIn", false);
        }
        return "dashboard";
    }

    @PostMapping("/dashboard/action")
    public String handleDashboardAction(@RequestParam("feature") String feature,
                                        HttpSession session,
                                        RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để sử dụng chức năng.");
            return "redirect:/login";
        }

        if ("Tìm trạm".equalsIgnoreCase(feature)) {
            return "redirect:/stations";
        }

        redirect.addFlashAttribute("dashboardMessage", "Bạn đã chọn chức năng: " + feature);
        return "redirect:/dashboard";
    }
}

