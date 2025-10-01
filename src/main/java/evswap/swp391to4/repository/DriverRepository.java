package evswap.swp391to4.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import evswap.swp391to4.entity.Driver;

public interface DriverRepository extends JpaRepository<Driver, Integer> {
    Optional<Driver> findByEmail(String email);
    boolean existsByPhone(String phone);
}

