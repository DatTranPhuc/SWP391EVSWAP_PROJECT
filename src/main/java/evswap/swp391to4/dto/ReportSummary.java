package evswap.swp391to4.dto;

public record ReportSummary(
        long totalDrivers,
        long verifiedDrivers,
        long newDriversThisMonth,
        long totalVehicles,
        long vehiclesRegisteredThisMonth,
        long totalStations,
        long activeStations
) {
}
