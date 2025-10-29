package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ApiResponse;
import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.service.AdminService;
import evswap.swp391to4.service.DriverService;
import evswap.swp391to4.service.StaffService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final DriverService driverService;
    private final StaffService staffService;
    private final AdminService adminService;
    private final DriverRepository driverRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> loginPage() {
        Map<String, Object> data = Map.of(
                "requiredFields", new String[]{"email", "password"},
                "submit", "POST /api/auth/login"
        );
        return ResponseEntity.ok(ApiResponse.success("Hiển thị form đăng nhập ở FE.", data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> request,
                                                                  HttpSession session) {
        String email = request.getOrDefault("email", "");
        String password = request.getOrDefault("password", "");
        if (email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>failure("Email và mật khẩu là bắt buộc."));
        }

        try {
            Driver driver = driverService.login(email, password);
            session.setAttribute("loggedInDriver", driver);
            Map<String, Object> data = new HashMap<>();
            data.put("role", "DRIVER");
            data.put("next", "/api/dashboard");
            data.put("displayName", driver.getFullName());
            return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công!", data));
        } catch (Exception ignored) {
            // continue to staff/admin
        }

        try {
            Staff staff = staffService.login(email, password);
            session.setAttribute("loggedInStaff", staff);
            Map<String, Object> data = new HashMap<>();
            data.put("role", "STAFF");
            data.put("next", "/api/staff/dashboard");
            data.put("displayName", staff.getFullName());
            return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công!", data));
        } catch (Exception ignored) {
            // continue to admin
        }

        try {
            Admin admin = adminService.login(email, password);
            session.setAttribute("loggedInAdmin", admin);
            Map<String, Object> data = new HashMap<>();
            data.put("role", "ADMIN");
            data.put("next", "/api/admin/dashboard");
            data.put("displayName", admin.getFullName());
            return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công!", data));
        } catch (Exception adminException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, Object>>failure(adminException.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.<Void>success("Đăng xuất thành công."));
    }

    @GetMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerPage() {
        Map<String, Object> data = Map.of(
                "requiredFields", new String[]{"email", "password", "fullName", "phone"},
                "submit", "POST /api/auth/register"
        );
        return ResponseEntity.ok(ApiResponse.success("Hiển thị form đăng ký ở FE.", data));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody Map<String, String> request) {
        String email = request.getOrDefault("email", "").trim();
        String password = request.getOrDefault("password", "").trim();
        String fullName = request.getOrDefault("fullName", "").trim();
        String phone = request.get("phone");

        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>failure("Email, mật khẩu và họ tên là bắt buộc."));
        }

        Driver driver = Driver.builder()
                .email(email)
                .passwordHash(password)
                .fullName(fullName)
                .phone(phone)
                .build();

        driverService.register(driver);

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("next", "/api/auth/verify-otp");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công! Vui lòng kiểm tra email để lấy OTP.", data));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPage() {
        Map<String, Object> data = Map.of(
                "requiredFields", new String[]{"email", "otp"},
                "submit", "POST /api/auth/verify-otp"
        );
        return ResponseEntity.ok(ApiResponse.success("Nhập OTP đã gửi tới email.", data));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.getOrDefault("email", "");
        String otp = request.getOrDefault("otp", "");
        if (email.isBlank() || otp.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>failure("Email và OTP là bắt buộc."));
        }

        Driver driver = driverService.verifyOtp(email, otp);
        Map<String, Object> data = new HashMap<>();
        data.put("driverId", driver.getDriverId());
        data.put("next", "/api/vehicles/register");
        return ResponseEntity.ok(ApiResponse.success("Xác minh email thành công!", data));
    }

    @GetMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> forgotPasswordForm() {
        Map<String, Object> data = Map.of(
                "requiredFields", new String[]{"email"},
                "submit", "POST /api/auth/forgot-password"
        );
        return ResponseEntity.ok(ApiResponse.success("Nhập email cần đặt lại mật khẩu.", data));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleForgotPassword(@RequestBody Map<String, String> request) {
        String email = request.getOrDefault("email", "");
        if (email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>failure("Email là bắt buộc."));
        }

        Optional<Driver> opt = driverRepository.findByEmail(email);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Map<String, Object>>failure("Không tìm thấy tài khoản với email này."));
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
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.<Map<String, Object>>failure("Không thể gửi email: " + ex.getMessage()));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("next", "/api/auth/reset-password");
        return ResponseEntity.ok(ApiResponse.success("Đã gửi mã xác thực đến email.", data));
    }

    @GetMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPasswordForm() {
        Map<String, Object> data = Map.of(
                "requiredFields", new String[]{"email", "otp", "newPassword"},
                "submit", "POST /api/auth/reset-password"
        );
        return ResponseEntity.ok(ApiResponse.success("Nhập email, OTP và mật khẩu mới.", data));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> handleResetPassword(@RequestBody Map<String, String> request) {
        String email = request.getOrDefault("email", "");
        String otp = request.getOrDefault("otp", "");
        String newPassword = request.getOrDefault("newPassword", "");

        if (email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Void>failure("Email, OTP và mật khẩu mới là bắt buộc."));
        }

        Optional<Driver> opt = driverRepository.findByEmail(email);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>failure("Email không hợp lệ."));
        }

        Driver driver = opt.get();

        if (driver.getEmailOtp() == null || driver.getOtpExpiry() == null
                || Instant.now().isAfter(driver.getOtpExpiry())
                || !driver.getEmailOtp().equals(otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>failure("Mã OTP không hợp lệ hoặc đã hết hạn."));
        }

        driver.setPasswordHash(passwordEncoder.encode(newPassword));
        driver.setEmailOtp(null);
        driver.setOtpExpiry(null);
        driverRepository.save(driver);

        return ResponseEntity.ok(ApiResponse.<Void>success("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới."));
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
