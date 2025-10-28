package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.DriverService;
import evswap.swp391to4.service.NotificationService;
import evswap.swp391to4.service.ReservationService;
import evswap.swp391to4.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DriverService driverService;
    private final VehicleService vehicleService;
    private final ReservationService reservationService;
    private final NotificationService notificationService; // Bạn cần tạo thêm service này

    /**
     * Trả về tổng quan dashboard của tài xế (profile, thống kê, vehicles, lịch sử swap, notification)
     */
    @GetMapping
    public ResponseEntity<?> getDashboard(@RequestHeader(name = "Driver-Id") Integer driverId) {
        if (driverId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn chưa đăng nhập"));
        }

        Driver driver = driverService.getDriverById(driverId);

        // Giả lập các API lấy dữ liệu:
        List<?> vehicles = vehicleService.getVehiclesForDriver(driverId);
        List<?> reservations = reservationService.getUpcomingReservations(driverId);
        List<?> notifications = notificationService.getNotificationsForDriver(driverId); // Cần service

        // Thêm các thống kê số lượng, trạng thái, v.v. nếu muốn
        int totalVehicles = vehicles.size();
        int totalReservations = reservations.size();
        int unreadNotifications = (int) notifications.stream().filter(noti -> !((Map)noti).get("isRead").equals(Boolean.TRUE)).count();

        return ResponseEntity.ok(Map.of(
                "loggedIn", true,
                "driver", Map.of(
                        "driverId", driver.getDriverId(),
                        "name", driver.getFullName(),
                        "email", driver.getEmail()
                ),
                "statistics", Map.of(
                        "totalVehicles", totalVehicles,
                        "totalReservations", totalReservations,
                        "unreadNotifications", unreadNotifications
                ),
                "vehicles", vehicles, // Mảng phương tiện/DTO
                "upcomingReservations", reservations, // Mảng lịch sử/DTO
                "notifications", notifications // Mảng thông báo/DTO
        ));
    }

    // Xử lý action trên dashboard như cũ, có thể mở rộng cho mobile
    @PostMapping("/action")
    public ResponseEntity<?> handleDashboardAction(@RequestParam("feature") String feature,
                                                   @RequestHeader(name = "Driver-Id") Integer driverId) {
        if (driverId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập để sử dụng chức năng."));
        }

        String normalizedFeature = feature == null ? "" : feature.trim();
        switch (normalizedFeature.toLowerCase()) {
            case "tổng quan":
                return ResponseEntity.ok(Map.of("dashboardMessage", "Bạn đang ở trang tổng quan EV SWAP."));
            case "phương tiện":
                return ResponseEntity.ok(Map.of("redirectTo", "/api/vehicles/manage"));
            case "đổi pin":
            case "tìm trạm":
                return ResponseEntity.ok(Map.of("redirectTo", "/api/reservations/schedule"));
            case "báo cáo":
                return ResponseEntity.ok(Map.of("dashboardMessage", "Chức năng Báo cáo sẽ sớm ra mắt."));
            case "tài khoản":
                return ResponseEntity.ok(Map.of("dashboardMessage", "Truy cập trang tài khoản trong phiên bản sắp tới."));
            case "hỗ trợ":
                return ResponseEntity.ok(Map.of("dashboardMessage", "Đội ngũ hỗ trợ sẽ sẵn sàng sau khi bạn đăng nhập."));
            default:
                return ResponseEntity.ok(Map.of("dashboardMessage", "Bạn đã chọn chức năng: " + normalizedFeature));
        }
    }
}
