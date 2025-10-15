package evswap.swp391to4.service;

import evswap.swp391to4.dto.PaymentRequest;
import evswap.swp391to4.dto.PaymentResponse;
import evswap.swp391to4.dto.PaymentStatusUpdateRequest;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.PaymentRepository;
import evswap.swp391to4.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DriverRepository driverRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản tài xế không tồn tại"));

        Reservation reservation = null;
        if (request.getReservationId() != null) {
            reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("Reservation không tồn tại"));
        }

        paymentRepository.findByProviderTxnId(request.getProviderTxnId())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Giao dịch đã tồn tại");
                });

        Payment payment = Payment.builder()
                .driver(driver)
                .reservation(reservation)
                .amount(request.getAmount())
                .method(request.getMethod())
                .status("pending")
                .currency(request.getCurrency())
                .providerTxnId(request.getProviderTxnId())
                .build();

        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    @Transactional
    public PaymentResponse updateStatus(Integer paymentId, PaymentStatusUpdateRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment không tồn tại"));

        if (request.getStatus() != null) {
            payment.setStatus(request.getStatus());
            if ("succeed".equalsIgnoreCase(request.getStatus())) {
                payment.setPaidAt(Instant.now());
            }
        }

        return toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsForDriver(Integer driverId) {
        return paymentRepository.findByDriverDriverIdOrderByPaidAtDesc(driverId).stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .driverId(payment.getDriver() != null ? payment.getDriver().getDriverId() : null)
                .driverName(payment.getDriver() != null ? payment.getDriver().getFullName() : null)
                .reservationId(payment.getReservation() != null ? payment.getReservation().getReservationId() : null)
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .currency(payment.getCurrency())
                .providerTxnId(payment.getProviderTxnId())
                .build();
    }
}
