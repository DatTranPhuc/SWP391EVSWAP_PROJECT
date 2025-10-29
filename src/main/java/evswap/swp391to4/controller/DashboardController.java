package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ApiResponse;
import evswap.swp391to4.entity.Driver;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> showDashboard(HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");

        Map<String, Object> payload = new HashMap<>();
        payload.put("loggedIn", driver != null);
        if (driver != null) {
            payload.put("driverName", driver.getFullName());
        }

        return ResponseEntity.ok(ApiResponse.success("Thông tin dashboard", payload));
    }

    @PostMapping("/action")
    public ResponseEntity<ApiResponse<Map<String, String>>> handleDashboardAction(@RequestBody Map<String, String> request,
                                                                                  HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            Map<String, String> data = Map.of("next", "/api/auth/login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, String>>failure("Vui lòng đăng nhập để sử dụng chức năng.").withData(data));
        }

        String normalizedFeature = request.getOrDefault("feature", "").trim();
        Map<String, String> data = new HashMap<>();
        data.put("selected", normalizedFeature);

        if ("Tổng quan".equalsIgnoreCase(normalizedFeature)) {
            data.put("next", "/api/dashboard");
            return ResponseEntity.ok(ApiResponse.success("Bạn đang ở trang tổng quan EV SWAP.", data));
        }

        if ("Phương tiện".equalsIgnoreCase(normalizedFeature)) {
            data.put("next", "/api/vehicles");
            return ResponseEntity.ok(ApiResponse.success("Đi tới trang quản lý phương tiện.", data));
        }

        if ("Đổi pin".equalsIgnoreCase(normalizedFeature) || "Tìm trạm".equalsIgnoreCase(normalizedFeature)) {
            data.put("next", "/api/reservations/schedule");
            return ResponseEntity.ok(ApiResponse.success("Đi tới đặt lịch đổi pin.", data));
        }

        if ("Báo cáo".equalsIgnoreCase(normalizedFeature)) {
            return ResponseEntity.ok(ApiResponse.success("Chức năng Báo cáo sẽ sớm ra mắt.", Map.of()));
        }

        if ("Tài khoản".equalsIgnoreCase(normalizedFeature)) {
            return ResponseEntity.ok(ApiResponse.success("Trang tài khoản sẽ được cập nhật trong phiên bản sắp tới.", Map.of()));
        }

        if ("Hỗ trợ".equalsIgnoreCase(normalizedFeature)) {
            return ResponseEntity.ok(ApiResponse.success("Đội ngũ hỗ trợ sẽ sẵn sàng sau khi bạn đăng nhập.", Map.of()));
        }

        return ResponseEntity.ok(ApiResponse.success("Bạn đã chọn chức năng: " + normalizedFeature, data));
    }
}

