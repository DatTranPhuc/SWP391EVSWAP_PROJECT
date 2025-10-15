package evswap.swp391to4.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.repository.DriverRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final DriverRepository driverRepository;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from "Bearer <token>"
            String token = authHeader.replace("Bearer ", "");
            
            // For demo purposes, we'll return the first driver or create a mock user
            Driver driver = driverRepository.findById(1).orElse(null);
            
            Map<String, Object> user = new HashMap<>();
            if (driver != null) {
                user.put("driverId", driver.getDriverId());
                user.put("email", driver.getEmail());
                user.put("fullName", driver.getFullName());
                user.put("role", "driver");
                user.put("emailVerified", driver.getEmailVerified());
                user.put("phone", driver.getPhone());
                user.put("createdAt", driver.getCreatedAt());
            } else {
                // Fallback mock user
                user.put("driverId", 1);
                user.put("email", "demo@example.com");
                user.put("fullName", "Demo User");
                user.put("role", "driver");
                user.put("emailVerified", true);
                user.put("phone", "0123456789");
                user.put("createdAt", java.time.Instant.now());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
    }
}
