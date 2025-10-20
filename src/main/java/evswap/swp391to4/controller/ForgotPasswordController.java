package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.repository.DriverRepository;
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
public class ForgotPasswordController {

    private final DriverRepository driverRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // ======= HIỂN THỊ TRANG NHẬP EMAIL =======
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    // ======= GỬI OTP ĐẾN EMAIL =======
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

    // ======= HIỂN THỊ TRANG ĐẶT LẠI MẬT KHẨU =======
    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(value = "email", required = false) String email,
                                    Model model) {
        model.addAttribute("email", email);
        return "reset-password";
    }

    // ======= XỬ LÝ ĐẶT LẠI MẬT KHẨU =======
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

    // ======= HÀM SINH MÃ OTP =======
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
