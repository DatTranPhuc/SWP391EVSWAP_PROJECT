package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Driver;
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
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirectAttributes.addFlashAttribute("loginRequired",
                    "Vui lòng đăng nhập để truy cập bảng điều khiển vận hành.");
            return "redirect:/login";
        }

        String driverName = driver.getFullName() != null ? driver.getFullName() : "Tài xế EV";
        String trimmed = driverName.trim();
        String driverInitial = trimmed.isEmpty() ? "E" : trimmed.substring(0, 1).toUpperCase();

        model.addAttribute("driverName", driverName);
        model.addAttribute("driverInitial", driverInitial);
        return "operations-console";
    }
}
