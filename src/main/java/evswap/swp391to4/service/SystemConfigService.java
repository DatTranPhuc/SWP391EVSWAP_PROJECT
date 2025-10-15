package evswap.swp391to4.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final BatteryRepository batteryRepository;
    private final StationRepository stationRepository;

    /**
     * Lấy danh sách trạng thái pin có thể có
     */
    public Map<String, Object> getBatteryStatuses() {
        Map<String, Object> statuses = new HashMap<>();
        statuses.put("statuses", List.of(
            Map.of("value", "using", "label", "Đang sử dụng", "color", "success"),
            Map.of("value", "charging", "label", "Đang sạc", "color", "warning"),
            Map.of("value", "maintenance", "label", "Bảo trì", "color", "danger"),
            Map.of("value", "retired", "label", "Ngừng sử dụng", "color", "secondary")
        ));
        return statuses;
    }

    /**
     * Lấy danh sách tình trạng pin dựa trên SOH
     */
    public Map<String, Object> getBatteryConditions() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("conditions", List.of(
            Map.of("minSoh", 90, "maxSoh", 100, "label", "Tuyệt vời", "color", "success"),
            Map.of("minSoh", 80, "maxSoh", 89, "label", "Tốt", "color", "info"),
            Map.of("minSoh", 70, "maxSoh", 79, "label", "Khá", "color", "warning"),
            Map.of("minSoh", 60, "maxSoh", 69, "label", "Trung bình", "color", "warning"),
            Map.of("minSoh", 0, "maxSoh", 59, "label", "Cần thay thế", "color", "danger")
        ));
        return conditions;
    }

    /**
     * Lấy danh sách trạng thái trạm
     */
    public Map<String, Object> getStationStatuses() {
        Map<String, Object> statuses = new HashMap<>();
        statuses.put("statuses", List.of(
            Map.of("value", "active", "label", "Hoạt động", "color", "success"),
            Map.of("value", "maintenance", "label", "Bảo trì", "color", "warning"),
            Map.of("value", "inactive", "label", "Không hoạt động", "color", "danger")
        ));
        return statuses;
    }

    /**
     * Lấy danh sách model xe phổ biến
     */
    public Map<String, Object> getVehicleModels() {
        Map<String, Object> models = new HashMap<>();
        models.put("models", List.of(
            "VinFast Klara",
            "VinFast Theon",
            "Yadea G5",
            "Yadea C1S",
            "Honda PCX Electric",
            "Yamaha NMAX Electric",
            "SYM E-Bike",
            "Piaggio Vespa Elettrica",
            "BMW C Evolution",
            "KTM E-Duke"
        ));
        return models;
    }

    /**
     * Lấy danh sách tên trạm mẫu
     */
    public Map<String, Object> getStationNameTemplates() {
        Map<String, Object> templates = new HashMap<>();
        templates.put("templates", List.of(
            "Trạm đổi pin Quận 1",
            "Trạm đổi pin Quận 2",
            "Trạm đổi pin Quận 3",
            "Trạm đổi pin Quận 7",
            "Trạm đổi pin Quận 10",
            "Trạm đổi pin Thủ Đức",
            "Trạm đổi pin Bình Thạnh",
            "Trạm đổi pin Gò Vấp",
            "Trạm đổi pin Tân Bình",
            "Trạm đổi pin Phú Nhuận"
        ));
        return templates;
    }

    /**
     * Lấy tất cả cấu hình hệ thống
     */
    public Map<String, Object> getAllSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("batteryStatuses", getBatteryStatuses());
        config.put("batteryConditions", getBatteryConditions());
        config.put("stationStatuses", getStationStatuses());
        config.put("vehicleModels", getVehicleModels());
        config.put("stationNameTemplates", getStationNameTemplates());
        return config;
    }
}
