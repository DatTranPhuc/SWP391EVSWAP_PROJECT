package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Admin;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminConsoleController {

    @GetMapping("/console")
    public String renderConsole(HttpSession session, RedirectAttributes redirectAttributes) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            redirectAttributes.addFlashAttribute("loginRequired", "Vui lòng đăng nhập với tài khoản quản trị.");
            return "redirect:/login";
        }
        return "admin-console";
    }
}
