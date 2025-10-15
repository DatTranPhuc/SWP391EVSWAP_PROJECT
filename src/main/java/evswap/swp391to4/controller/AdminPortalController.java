package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.repository.AdminRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPortalController {

    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() { return "admin/admin-login"; }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes ra) {
        Admin admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin không tồn tại"));
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            ra.addFlashAttribute("loginError", "Sai mật khẩu");
            return "redirect:/admin/login";
        }
        session.setAttribute("loggedInAdmin", admin);
        ra.addFlashAttribute("loginSuccess", "Chào " + admin.getFullName());
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, RedirectAttributes ra) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            ra.addFlashAttribute("loginRequired", "Vui lòng đăng nhập admin trước.");
            return "redirect:/admin/login";
        }
        model.addAttribute("username", admin.getFullName());
        return "admin/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("logoutMessage", "Đăng xuất thành công.");
        return "redirect:/admin/login";
    }
}
