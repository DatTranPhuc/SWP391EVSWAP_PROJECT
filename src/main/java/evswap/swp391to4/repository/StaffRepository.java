package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Optional<Staff> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByIsActiveTrue();
}
