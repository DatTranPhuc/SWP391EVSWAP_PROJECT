package evswap.swp391to4.service.impl;

import evswap.swp391to4.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public List<?> getNotificationsForDriver(Integer driverId) {
        // Demo trả danh sách thông báo mock - bạn thay thành lấy từ db/repo khi có dữ liệu
        return List.of(
                Map.of("id", 1, "driverId", driverId, "title", "Pin đã được đổi thành công", "isRead", false, "createdAt", "2025-10-28T14:00:00"),
                Map.of("id", 2, "driverId", driverId, "title", "Bạn có lịch hẹn đổi pin mới", "isRead", true, "createdAt", "2025-10-28T13:40:00")
        );
    }
}
