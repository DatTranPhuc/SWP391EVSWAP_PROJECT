package evswap.swp391to4.entity;

public enum VehicleType {
    FOUR_WHEEL("Xe 4 bánh", "EVS Car Pack 100kWh"),
    TWO_WHEEL("Xe 2 bánh (Scooter)", "EVS Scooter Pack 48V");

    private final String displayName;
    private final String defaultBatteryModel;

    VehicleType(String displayName, String defaultBatteryModel) {
        this.displayName = displayName;
        this.defaultBatteryModel = defaultBatteryModel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultBatteryModel() {
        return defaultBatteryModel;
    }
}
