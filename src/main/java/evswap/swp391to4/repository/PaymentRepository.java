package evswap.swp391to4.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.entity.Reservation;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByDriverOrderByPaidAtDesc(Driver driver);
    List<Payment> findByReservation(Reservation reservation);
    Optional<Payment> findByProviderTxnId(String providerTxnId);
    Optional<Payment> findByPaymentIdAndDriver(Integer paymentId, Driver driver);
    Optional<Payment> findByOrderCode(String orderCode);
}


