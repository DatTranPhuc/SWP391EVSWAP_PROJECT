package evswap.swp391to4.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final DriverService driverService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String fullName = body.getOrDefault("fullName", body.getOrDefault("full_name", ""));
        String phone = body.get("phone");

        try {
            Driver driver = Driver.builder()
                    .email(email)
                    .passwordHash(password)
                    .fullName(fullName)
                    .phone(phone)
                    .build();
            Driver saved = driverService.register(driver);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("email", saved.getEmail());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        try {
            Driver driver = driverService.verifyOtp(email, otp);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("driverId", driver.getDriverId());
            res.put("fullName", driver.getFullName());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        try {
            Driver driver = driverService.login(email, password);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            Map<String, Object> user = new HashMap<>();
            user.put("driverId", driver.getDriverId());
            user.put("email", driver.getEmail());
            user.put("fullName", driver.getFullName());
            user.put("role", "driver");
            res.put("user", user);
            res.put("token", "demo-token-" + driver.getDriverId()); // Simple token for demo
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

}


