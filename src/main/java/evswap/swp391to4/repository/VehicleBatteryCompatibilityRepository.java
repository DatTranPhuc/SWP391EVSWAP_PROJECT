package evswap.swp391to4.repository;

import evswap.swp391to4.entity.VehicleBatteryCompatibility;
import evswap.swp391to4.entity.VehicleBatteryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleBatteryCompatibilityRepository extends JpaRepository<VehicleBatteryCompatibility, VehicleBatteryId> {
    List<VehicleBatteryCompatibility> findByVehicleVehicleId(Integer vehicleId);
    List<VehicleBatteryCompatibility> findByBatteryBatteryId(Integer batteryId);
}
