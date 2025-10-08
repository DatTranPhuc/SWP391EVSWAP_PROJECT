package evswap.swp391to4.controller;

import evswap.swp391to4.dto.RegisterRequest;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DriverService driverService;

    // ===== REGISTER =====
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Driver driver = Driver.builder()
                    .email(request.getEmail())
                    .passwordHash(request.getPassword())
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .build();

            driverService.register(driver);
            return ResponseEntity.ok("Đăng ký thành công! Vui lòng kiểm tra email để lấy OTP.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== VERIFY OTP =====
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            driverService.verifyOtp(email, otp);
            return ResponseEntity.ok("Xác minh email thành công! Vui lòng đăng nhập.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== LOGIN =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            Driver driver = driverService.login(email, password);
            return ResponseEntity.ok("Đăng nhập thành công! Xin chào " + driver.getFullName());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
