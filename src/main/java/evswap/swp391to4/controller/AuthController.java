package evswap.swp391to4.controller;

import evswap.swp391to4.dto.*;
import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.AdminService;
import evswap.swp391to4.service.DriverService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DriverService driverService;
    private final AdminService adminService;

    // ===== REGISTER =====
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            Driver driver = driverService.register(req);
            RegisterResponse resp = new RegisterResponse(
                    driver.getEmail(),
                    driver.getFullName(),
                    "Vui lòng kiểm tra email để lấy OTP"
            );
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new RegisterResponse(null, null, e.getMessage()));
        }
    }

    // ===== VERIFY OTP =====
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        try {
            Driver driver = driverService.verifyOtp(req.getEmail(), req.getOtp());
            return ResponseEntity.ok(new LoginResponse(
                    driver.getEmail(),
                    driver.getFullName(),
                    null,
                    "Xác minh email thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new LoginResponse(null, null, null, e.getMessage()));
        }
    }

    // ===== LOGIN =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        try {
            Driver driver = driverService.login(req.getEmail(), req.getPassword());
            session.setAttribute("loggedInDriver", driver);
            return ResponseEntity.ok(new LoginResponse(
                    driver.getEmail(),
                    driver.getFullName(),
                    null,
                    "Login thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new LoginResponse(null, null, null, e.getMessage()));
        }
    }

    // ===== LOGOUT =====
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new LoginResponse(null, null, null, "Đăng xuất thành công"));
    }

    // ===== LOGIN (Admin) - ĐÃ SỬA LẠI HOÀN CHỈNH =====
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest req, HttpSession session) {
        try {
            Admin admin = adminService.login(req.getEmail(), req.getPassword());
            session.setAttribute("loggedInAdmin", admin);

            LoginResponse resp = new LoginResponse(
                    admin.getEmail(),
                    admin.getFullName(),
                    null, // token nếu muốn triển khai JWT sau
                    "Login thành công"
            );

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new LoginResponse(null, null, null, e.getMessage())
            );
        }
    }

}
