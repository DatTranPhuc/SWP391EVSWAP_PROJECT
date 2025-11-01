package evswap.swp391to4.entity;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Chuẩn hoá các nhóm xe để xác định loại pin tương thích.
 */
public enum VehicleType {
    CITY_48V(
            "Xe điện 48V phổ thông",
            "VinFast Feliz, Pega, Dibao...",
            List.of("MODEL-A")
    ),
    PERFORMANCE_60V(
            "Xe điện 60V công suất cao",
            "Dat Bike Weaver, VinFast Vento...",
            List.of("MODEL-B")
    ),
    UNIVERSAL(
            "Khác / chưa xác định",
            "Cho phép lựa chọn mọi model pin hiện có",
            List.of()
    );

    private final String displayName;
    private final String description;
    private final List<String> compatibleBatteryModels;

    VehicleType(String displayName, String description, List<String> compatibleBatteryModels) {
        this.displayName = displayName;
        this.description = description;
        this.compatibleBatteryModels = compatibleBatteryModels;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getCompatibleBatteryModels() {
        return Collections.unmodifiableList(compatibleBatteryModels);
    }

    public String getCompatibleBatteryLabel() {
        if (compatibleBatteryModels.isEmpty()) {
            return "Mọi model pin";
        }
        return String.join(", ", compatibleBatteryModels);
    }

    public boolean supportsBatteryModel(String batteryModel) {
        if (batteryModel == null || batteryModel.isBlank()) {
            return false;
        }
        if (compatibleBatteryModels.isEmpty()) {
            return true;
        }
        return compatibleBatteryModels.stream()
                .anyMatch(model -> model.equalsIgnoreCase(batteryModel));
    }

    public static VehicleType fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return UNIVERSAL;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (VehicleType value : values()) {
            if (value.name().equals(normalized)) {
                return value;
            }
        }
        return UNIVERSAL;
    }
}

