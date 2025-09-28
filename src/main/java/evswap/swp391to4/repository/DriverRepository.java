package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Integer> {
    Optional<Driver> findByEmail(String email);
}

