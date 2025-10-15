package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Driver;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> showDashboard(HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        Map<String, Object> payload = new HashMap<>();
        if (driver != null) {
            payload.put("driverName", driver.getFullName());
            payload.put("loggedIn", true);
        } else {
            payload.put("loggedIn", false);
        }
        return ResponseEntity.ok(payload);
    }

    @PostMapping("/action")
    public ResponseEntity<Map<String, String>> handleDashboardAction(@RequestBody DashboardActionRequest request,
                                                                     HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Vui lòng đăng nhập để sử dụng chức năng."));
        }

        String normalizedFeature = request.feature() == null ? "" : request.feature().trim();
        Map<String, String> response = new HashMap<>();

        if ("Tổng quan".equalsIgnoreCase(normalizedFeature)) {
            response.put("message", "Bạn đang ở trang tổng quan EV SWAP.");
            response.put("redirect", "/api/dashboard");
            return ResponseEntity.ok(response);
        }

        if ("Phương tiện".equalsIgnoreCase(normalizedFeature)) {
            response.put("message", "Đi tới quản lý phương tiện");
            response.put("redirect", "/api/drivers/" + driver.getDriverId() + "/vehicles/overview");
            return ResponseEntity.ok(response);
        }

        if ("Tìm trạm".equalsIgnoreCase(normalizedFeature)) {
            response.put("message", "Chức năng Tìm trạm đang được phát triển.");
            return ResponseEntity.ok(response);
        }

        if ("Báo cáo".equalsIgnoreCase(normalizedFeature)) {
            response.put("message", "Chức năng Báo cáo sẽ sớm ra mắt.");
            return ResponseEntity.ok(response);
        }

        if ("Tài khoản".equalsIgnoreCase(normalizedFeature)) {
            response.put("message", "Truy cập trang tài khoản trong phiên bản sắp tới.");
            return ResponseEntity.ok(response);
        }

        if ("Hỗ trợ".equalsIgnoreCase(normalizedFeature)) {
            response.put("message", "Đội ngũ hỗ trợ sẽ sẵn sàng sau khi bạn đăng nhập.");
            return ResponseEntity.ok(response);
        }

        response.put("message", "Bạn đã chọn chức năng: " + normalizedFeature);
        return ResponseEntity.ok(response);
    }

    public record DashboardActionRequest(String feature) {
    }
}

