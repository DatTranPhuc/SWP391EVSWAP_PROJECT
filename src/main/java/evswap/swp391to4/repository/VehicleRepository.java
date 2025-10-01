package evswap.swp391to4.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import evswap.swp391to4.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    boolean existsByVin(String vin);
    Optional<Vehicle> findByVin(String vin);
}


