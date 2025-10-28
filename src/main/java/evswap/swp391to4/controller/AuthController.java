package evswap.swp391to4.controller;

import evswap.swp391to4.dto.LoginRequest;
import evswap.swp391to4.dto.LoginResponse;
import evswap.swp391to4.dto.RegisterRequest;
import evswap.swp391to4.dto.RegisterResponse;
import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.service.AdminService;
import evswap.swp391to4.service.DriverService;
import evswap.swp391to4.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DriverService driverService;
    private final StaffService staffService;
    private final AdminService adminService;
    private final DriverRepository driverRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // ===== LOGIN =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Driver> driverOpt = driverService.login(request.getEmail(), request.getPassword());
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            return ResponseEntity.ok(LoginResponse.builder()
                    .email(driver.getEmail())
                    .fullName(driver.getFullName())
                    .token("your-jwt-token")
                    .message("Driver login thành công!").build());
        }

        Optional<Staff> staffOpt = staffService.login(request.getEmail(), request.getPassword());
        if (staffOpt.isPresent()) {
            Staff staff = staffOpt.get();
            return ResponseEntity.ok(LoginResponse.builder()
                    .email(staff.getEmail())
                    .fullName(staff.getFullName())
                    .token("your-jwt-token")
                    .message("Staff login thành công!").build());
        }

        Optional<Admin> adminOpt = Optional.ofNullable(adminService.login(request.getEmail(), request.getPassword()));
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            return ResponseEntity.ok(LoginResponse.builder()
                    .email(admin.getEmail())
                    .fullName(admin.getFullName())
                    .token("your-jwt-token")
                    .message("Admin login thành công!").build());
        }
        return ResponseEntity.badRequest().body("Sai thông tin đăng nhập!");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Bạn đã đăng xuất thành công.");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated @RequestBody RegisterRequest request) {
        try {
            Driver driver = driverService.registerDriver(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFullName()
            );
            return ResponseEntity.ok(RegisterResponse.builder()
                    .email(driver.getEmail())
                    .fullName(driver.getFullName())
                    .message("Đăng ký thành công! Vui lòng xác minh email.").build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email,
                                       @RequestParam String otp) {
        boolean result = driverService.verifyOtp(email, otp);
        if (result) {
            return ResponseEntity.ok("Xác minh email thành công! Vui lòng đăng ký phương tiện.");
        }
        return ResponseEntity.badRequest().body("Xác thực thất bại: OTP không hợp lệ hoặc hết hạn.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> handleForgotPassword(@RequestParam("email") String email) {
        Optional<Driver> opt = driverRepository.findByEmail(email);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy tài khoản với email này.");
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
            return ResponseEntity.badRequest().body("Không thể gửi email: " + ex.getMessage());
        }
        return ResponseEntity.ok("Đã gửi mã xác thực đến email. Vui lòng kiểm tra hộp thư.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> handleResetPassword(@RequestParam("email") String email,
                                                 @RequestParam("otp") String otp,
                                                 @RequestParam("newPassword") String newPassword) {
        Optional<Driver> opt = driverRepository.findByEmail(email);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email không hợp lệ.");
        }
        Driver driver = opt.get();
        if (driver.getEmailOtp() == null || driver.getOtpExpiry() == null
                || Instant.now().isAfter(driver.getOtpExpiry())
                || !driver.getEmailOtp().equals(otp)) {
            return ResponseEntity.badRequest().body("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
        driver.setPasswordHash(passwordEncoder.encode(newPassword));
        driver.setEmailOtp(null);
        driver.setOtpExpiry(null);
        driverRepository.save(driver);

        return ResponseEntity.ok("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.");
    }

    // ===== UTIL =====
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
