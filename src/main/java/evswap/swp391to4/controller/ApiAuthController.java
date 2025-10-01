package evswap.swp391to4.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.dto.LoginRequest;
import evswap.swp391to4.dto.RegisterRequest;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.service.DriverService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiAuthController {

    private final DriverService driverService;
    private final DriverRepository driverRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            Driver driver = Driver.builder()
                    .email(req.getEmail())
                    .passwordHash(req.getPassword())
                    .fullName(req.getFullName())
                    .phone(req.getPhone())
                    .build();
            driverService.register(driver);

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("email", req.getEmail());
            return ResponseEntity.created(URI.create("/verify")).body(body);
        } catch (RuntimeException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Đăng ký thất bại";
            String lower = msg.toLowerCase();
            if (lower.contains("email")) msg = "Email đã được đăng ký";
            else if (lower.contains("phone") || lower.contains("điện thoại")) msg = "Số điện thoại đã được sử dụng";
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", msg);
            return ResponseEntity.badRequest().body(error);
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Lỗi hệ thống. Vui lòng thử lại sau.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            driverService.login(req.getEmail(), req.getPassword());
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("email", req.getEmail());
            return ResponseEntity.ok(body);
        } catch (RuntimeException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Lỗi hệ thống. Vui lòng thử lại sau.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> req) {
        try {
            String email = req.get("email");
            String otp = req.get("otp");
            driverService.verifyOtp(email, otp);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("email", email);
            return ResponseEntity.ok(body);
        } catch (RuntimeException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Lỗi hệ thống. Vui lòng thử lại sau.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Dev helper: get OTP for a given email (do NOT enable in production)
    @PostMapping("/dev/get-otp")
    public ResponseEntity<?> getOtp(@RequestBody Map<String, String> req) {
        try {
            String email = req.get("email");
            Driver driver = driverRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("email", email);
            body.put("otp", driver.getEmailOtp());
            body.put("otpExpiry", driver.getOtpExpiry());
            return ResponseEntity.ok(body);
        } catch (RuntimeException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Server error: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}


