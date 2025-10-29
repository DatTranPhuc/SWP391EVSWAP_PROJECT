package evswap.swp391to4.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    Optional<Vehicle> findByVin(String vin);
    Optional<Vehicle> findByPlateNumber(String plateNumber);
    List<Vehicle> findByDriverDriverIdOrderByCreatedAtDesc(Integer driverId);
    List<Vehicle> findByDriver(Driver driver);
}
