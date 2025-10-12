package evswap.swp391to4.dto;

/**
 * Thống kê tổng quan hiển thị trên trang dashboard.
 */
public record DashboardStats(
        long totalStations,
        long activeStations,
        long totalDrivers,
        long totalVehicles,
        long activeStaff
) {
}
