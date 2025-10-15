package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByDriverDriverIdOrderByPaidAtDesc(Integer driverId);
    Optional<Payment> findByProviderTxnId(String providerTxnId);
}
