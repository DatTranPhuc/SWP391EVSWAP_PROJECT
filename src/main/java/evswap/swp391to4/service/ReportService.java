package evswap.swp391to4.service;

import evswap.swp391to4.dto.ReportSummary;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;

    @Transactional(readOnly = true)
    public ReportSummary getReportSummary() {
        Instant startOfCurrentMonth = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        long totalDrivers = driverRepository.count();
        long verifiedDrivers = driverRepository.countByEmailVerifiedTrue();
        long newDriversThisMonth = driverRepository.countByCreatedAtAfter(startOfCurrentMonth);

        long totalVehicles = vehicleRepository.count();
        long vehiclesRegisteredThisMonth = vehicleRepository.countByCreatedAtAfter(startOfCurrentMonth);

        long totalStations = stationRepository.count();
        long activeStations = stationRepository.countByStatusIgnoreCase("active");

        return new ReportSummary(
                totalDrivers,
                verifiedDrivers,
                newDriversThisMonth,
                totalVehicles,
                vehiclesRegisteredThisMonth,
                totalStations,
                activeStations
        );
    }
}
