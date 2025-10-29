package evswap.swp391to4.service;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment createPayment(Driver driver, Reservation reservation, BigDecimal amount, String method, String status) {
        Payment payment = Payment.builder()
                .driver(driver)
                .reservation(reservation)
                .amount(amount)
                .method(method)
                .status(status)
                .paidAt(Instant.now())
                .currency("VND")
                .providerTxnId("FAKE-" + System.currentTimeMillis())
                .build();
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment simulatePay(Driver driver, Reservation reservation, BigDecimal amount) {
        return createPayment(driver, reservation, amount, "wallet", "succeed");
    }

    @Transactional
    public Payment simulateTopUp(Driver driver, BigDecimal amount) {
        return createPayment(driver, null, amount, "wallet", "succeed");
    }

    @Transactional
    public Payment simulateRefund(Driver driver, Reservation reservation, BigDecimal amount) {
        return createPayment(driver, reservation, amount, "wallet", "refunded");
    }
}


