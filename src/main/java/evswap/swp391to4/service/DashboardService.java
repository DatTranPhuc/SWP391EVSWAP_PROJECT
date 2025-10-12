package evswap.swp391to4.service;

import evswap.swp391to4.dto.DashboardStats;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.StaffRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StationRepository stationRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final StaffRepository staffRepository;

    public DashboardStats getDashboardStats() {
        long totalStations = stationRepository.count();
        long activeStations = stationRepository.countByStatusIgnoreCase("active");
        long totalDrivers = driverRepository.count();
        long totalVehicles = vehicleRepository.count();
        long activeStaff = staffRepository.countByIsActiveTrue();
        return new DashboardStats(totalStations, activeStations, totalDrivers, totalVehicles, activeStaff);
    }

    public long countVehiclesForDriver(Integer driverId) {
        if (driverId == null) {
            return 0L;
        }
        return vehicleRepository.countByDriverDriverId(driverId);
    }
}
