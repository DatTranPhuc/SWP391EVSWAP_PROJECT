package evswap.swp391to4.entity;

public enum VehicleType {
    FOUR_WHEEL("Ô tô 4 bánh"),
    TWO_WHEEL("Xe máy điện / Scooter");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
