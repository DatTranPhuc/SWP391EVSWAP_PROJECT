package evswap.swp391to4.controller;

import evswap.swp391to4.dto.LoginRequest;
import evswap.swp391to4.dto.LoginResponse;
import evswap.swp391to4.dto.RegisterRequest;
import evswap.swp391to4.dto.RegisterResponse;
import evswap.swp391to4.dto.VerifyOtpRequest;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.DriverService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DriverService driverService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        try {
            Driver driver = driverService.login(request.getEmail(), request.getPassword());
            session.setAttribute("loggedInDriver", driver);
            return ResponseEntity.ok(new LoginResponse(
                    driver.getDriverId(),
                    driver.getEmail(),
                    driver.getFullName(),
                    null,
                    "Login thành công!"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Bạn đã đăng xuất thành công."));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            Driver driver = Driver.builder()
                    .email(request.getEmail())
                    .passwordHash(request.getPassword())
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .build();

            driverService.register(driver);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponse(driver.getEmail(), driver.getFullName(),
                            "Đăng ký thành công! Vui lòng kiểm tra email để lấy OTP"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request, HttpSession session) {
        try {
            Driver driver = driverService.verifyOtp(request.getEmail(), request.getOtp());
            session.setAttribute("loggedInDriver", driver);
            return ResponseEntity.ok(Map.of(
                    "message", "Xác minh email thành công! Vui lòng đăng ký phương tiện.",
                    "driverId", driver.getDriverId(),
                    "fullName", driver.getFullName()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage(), "email", request.getEmail()));
        }
    }
}
