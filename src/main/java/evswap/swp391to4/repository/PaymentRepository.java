package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // Spring Data JPA sẽ tự động cung cấp các hàm save(), findById(), v.v.
}