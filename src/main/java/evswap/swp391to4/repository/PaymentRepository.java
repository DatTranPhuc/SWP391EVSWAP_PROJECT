package evswap.swp391to4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import evswap.swp391to4.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
