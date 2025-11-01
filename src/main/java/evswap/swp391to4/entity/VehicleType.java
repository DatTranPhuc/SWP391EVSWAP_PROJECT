package evswap.swp391to4.entity;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

/**
 * Chuẩn hoá các nhóm xe để xác định loại pin tương thích.
 */
public enum VehicleType {
    CITY_48V(
            "Xe điện 48V phổ thông",
            "VinFast Feliz, Pega, Dibao...",
            List.of("MODEL-A"),
            asList("CITY_48V", "CITY-48V", "CITY 48V", "48V", "48-V", "XE 48V")
    ),
    PERFORMANCE_60V(
            "Xe điện 60V công suất cao",
            "Dat Bike Weaver, VinFast Vento...",
            List.of("MODEL-B"),
            asList("PERFORMANCE_60V", "PERFORMANCE-60V", "PERFORMANCE 60V", "60V", "XE 60V")
    ),
    UNIVERSAL(
            "Khác / chưa xác định",
            "Cho phép lựa chọn mọi model pin hiện có",
            List.of(),
            asList("UNIVERSAL", "UNKNOWN", "OTHER", "KHAC", "KHÁC", "NA", "NONE")
    );

    private final String displayName;
    private final String description;
    private final List<String> compatibleBatteryModels;
    private final List<String> aliases;

    VehicleType(String displayName, String description, List<String> compatibleBatteryModels, List<String> aliases) {
        this.displayName = displayName;
        this.description = description;
        this.compatibleBatteryModels = List.copyOf(compatibleBatteryModels);
        this.aliases = List.copyOf(aliases);
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

    public List<String> getAliases() {
        return Collections.unmodifiableList(aliases);
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
        String normalized = normalizeKey(raw);
        for (VehicleType value : values()) {
            if (normalizeKey(value.name()).equals(normalized)) {
                return value;
            }
            if (normalizeKey(value.displayName).equals(normalized)) {
                return value;
            }
            for (String alias : value.aliases) {
                if (normalizeKey(alias).equals(normalized)) {
                    return value;
                }
            }
        }
        return UNIVERSAL;
    }

    private static String normalizeKey(String input) {
        String trimmed = input.trim().toUpperCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(trimmed.length());
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}

