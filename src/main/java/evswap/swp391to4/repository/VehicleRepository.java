package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    Optional<Vehicle> findByVin(String vin);
    Optional<Vehicle> findByPlateNumber(String plateNumber);
    List<Vehicle> findByDriverDriverIdOrderByCreatedAtDesc(Integer driverId);

    long countByCreatedAtAfter(Instant createdAt);
}
