package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.service.AdminService;
import evswap.swp391to4.service.DriverService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final DriverService driverService;
    private final AdminService adminService;
    private final DriverRepository driverRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // ===== LOGIN =====
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirect) {
        try {
            Driver driver = driverService.login(email, password);
            session.setAttribute("loggedInDriver", driver);
            redirect.addFlashAttribute("loginSuccess", "Login thành công! Chào " + driver.getFullName());
            return "redirect:/dashboard";
        } catch (Exception driverException) {
            try {
                Admin admin = adminService.login(email, password);
                session.setAttribute("loggedInAdmin", admin);
                redirect.addFlashAttribute("loginSuccess", "Admin login thành công! Chào " + admin.getFullName());
                return "redirect:/admin/dashboard";
            } catch (Exception adminException) {
                redirect.addFlashAttribute("loginError", adminException.getMessage());
                return "redirect:/login";
            }
        }
    }

    // ===== LOGOUT =====
    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        redirect.addFlashAttribute("logoutMessage", "Bạn đã đăng xuất thành công.");
        return "redirect:/login";
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
            redirect.addFlashAttribute("email", email);
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
            Driver driver = driverService.verifyOtp(email, otp);
            redirect.addFlashAttribute("verifySuccess", "Xác minh email thành công! Vui lòng đăng ký phương tiện.");
            return "redirect:/vehicles/register?driverId=" + driver.getDriverId();
        } catch (Exception e) {
            redirect.addFlashAttribute("verifyError", e.getMessage());
            redirect.addFlashAttribute("email", email);
            return "redirect:/verify";
        }
    }

    // ===== FORGOT PASSWORD =====
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") @NotBlank String email,
                                       RedirectAttributes redirect) {
        Optional<Driver> opt = driverRepository.findByEmail(email);
        if (opt.isEmpty()) {
            redirect.addFlashAttribute("error", "Không tìm thấy tài khoản với email này.");
            return "redirect:/forgot-password";
        }

        Driver driver = opt.get();
        String otp = generateOtp();
        driver.setEmailOtp(otp);
        driver.setOtpExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
        driverRepository.save(driver);

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(driver.getEmail());
            msg.setSubject("Yêu cầu đặt lại mật khẩu - EV SWAP");
            msg.setText("Xin chào " + driver.getFullName() + ",\n\n"
                    + "Mã đặt lại mật khẩu của bạn là: " + otp
                    + "\nMã có hiệu lực trong 10 phút.\n\nNếu bạn không yêu cầu, hãy bỏ qua email này.\n\nTrân trọng,\nEV SWAP Team");
            mailSender.send(msg);
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "Không thể gửi email: " + ex.getMessage());
            return "redirect:/forgot-password";
        }

        redirect.addFlashAttribute("success", "Đã gửi mã xác thực đến email. Vui lòng kiểm tra hộp thư.");
        return "redirect:/reset-password?email=" + email;
    }

    // ===== RESET PASSWORD =====
    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(value = "email", required = false) String email,
                                    Model model) {
        model.addAttribute("email", email);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("email") @NotBlank String email,
                                      @RequestParam("otp") @NotBlank String otp,
                                      @RequestParam("newPassword") @NotBlank String newPassword,
                                      RedirectAttributes redirect) {
        Optional<Driver> opt = driverRepository.findByEmail(email);
        if (opt.isEmpty()) {
            redirect.addFlashAttribute("error", "Email không hợp lệ.");
            return "redirect:/reset-password?email=" + email;
        }

        Driver driver = opt.get();

        if (driver.getEmailOtp() == null || driver.getOtpExpiry() == null
                || Instant.now().isAfter(driver.getOtpExpiry())
                || !driver.getEmailOtp().equals(otp)) {
            redirect.addFlashAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn.");
            return "redirect:/reset-password?email=" + email;
        }

        driver.setPasswordHash(passwordEncoder.encode(newPassword));
        driver.setEmailOtp(null);
        driver.setOtpExpiry(null);
        driverRepository.save(driver);

        redirect.addFlashAttribute("success", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.");
        return "redirect:/login";
    }

    // ===== UTIL =====
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
