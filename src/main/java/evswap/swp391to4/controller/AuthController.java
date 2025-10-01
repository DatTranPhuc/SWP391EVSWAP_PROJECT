package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final DriverService driverService;

    // ===== LOGIN =====
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        RedirectAttributes redirect) {
        try {
            Driver driver = driverService.login(email, password);
            redirect.addFlashAttribute("loginSuccess", "Login thành công! Chào " + driver.getFullName());
            return "redirect:/dashboard"; // Thay bằng trang sau login
        } catch (Exception e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        }
    }

    // ===== REGISTER =====
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           @RequestParam(required = false) String phone,
                           RedirectAttributes redirect) {
        try {
            Driver driver = Driver.builder()
                    .email(email)
                    .passwordHash(password)
                    .fullName(fullName)
                    .phone(phone)
                    .build();

            driverService.register(driver);
            redirect.addFlashAttribute("registerSuccess", "Đăng ký thành công! Vui lòng kiểm tra email để lấy OTP");
            redirect.addFlashAttribute("email", email); // Dùng trong verify.html
            return "redirect:/verify";
        } catch (Exception e) {
            redirect.addFlashAttribute("registerError", e.getMessage());
            return "redirect:/register";
        }
    }

    // ===== VERIFY OTP =====
    @GetMapping("/verify")
    public String verifyPage() {
        return "verify";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            RedirectAttributes redirect) {
        try {
            driverService.verifyOtp(email, otp);
            redirect.addFlashAttribute("verifySuccess", "Xác minh email thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("verifyError", e.getMessage());
            redirect.addFlashAttribute("email", email); // giữ lại email để hiển thị form
            return "redirect:/verify";
        }
    }
}
