package evswap.swp391to4.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final DriverRepository driverRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Integer driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài xế"));

        List<Payment> payments = paymentRepository.findByDriverOrderByPaidAtDesc(driver);

        BigDecimal balance = BigDecimal.ZERO;
        for (Payment p : payments) {
            if (!"succeed".equalsIgnoreCase(p.getStatus()) &&
                !"refunded".equalsIgnoreCase(p.getStatus())) {
                continue;
            }
            if (p.getReservation() == null && "succeed".equalsIgnoreCase(p.getStatus())) {
                balance = balance.add(p.getAmount());
            } else if (p.getReservation() != null && "succeed".equalsIgnoreCase(p.getStatus())) {
                balance = balance.subtract(p.getAmount());
            } else if ("refunded".equalsIgnoreCase(p.getStatus())) {
                balance = balance.add(p.getAmount());
            }
        }
        return balance.max(BigDecimal.ZERO);
    }
}


