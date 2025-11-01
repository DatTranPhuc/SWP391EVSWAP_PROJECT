package evswap.swp391to4.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.entity.VehicleBatteryCompatibility;

@Repository
public interface VehicleBatteryCompatibilityRepository extends JpaRepository<VehicleBatteryCompatibility, Object> {
    List<VehicleBatteryCompatibility> findByVehicle(Vehicle vehicle);
    List<VehicleBatteryCompatibility> findByBattery(Battery battery);
    
    /**
     * Kiểm tra xe có tương thích với model pin không
     */
    boolean existsByVehicleVehicleIdAndBatteryModel(Integer vehicleId, String batteryModel);
}


