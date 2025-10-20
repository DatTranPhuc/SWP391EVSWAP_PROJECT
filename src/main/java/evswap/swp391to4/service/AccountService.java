package evswap.swp391to4.service;

import evswap.swp391to4.dto.AccountViewResponse;
import evswap.swp391to4.dto.PaymentHistoryResponse;
import evswap.swp391to4.dto.QrCodeResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final DriverRepository driverRepo;
    private final PaymentRepository paymentRepo;
    private final PaymentGatewayService paymentGatewayService; // Đảm bảo đã inject service này

    // Định dạng tiền tệ và ngày giờ
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));


    public AccountViewResponse getAccountDetails(Integer driverId) {
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài xế với ID: " + driverId));

        return AccountViewResponse.builder()
                .driverName(driver.getFullName())
                .email(driver.getEmail())
                .formattedBalance(CURRENCY_FORMATTER.format(driver.getBalance()))
                .build();
    }


    public List<PaymentHistoryResponse> getPaymentHistory(Integer driverId) {
        return paymentRepo.findByDriver_DriverIdOrderByPaidAtDesc(driverId).stream()
                .map(this::mapPaymentToResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public void addFunds(Integer driverId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải là một số dương.");
        }

        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài xế với ID: " + driverId));

        driver.setBalance(driver.getBalance().add(amount));
        driverRepo.save(driver);

        Payment payment = Payment.builder()
                .driver(driver)
                .amount(amount)
                .status("succeed")
                .method("ewallet")
                .paidAt(Instant.now())
                .currency("VND")
                .build();
        paymentRepo.save(payment);
    }


    @Transactional
    public QrCodeResponse initiateQrPayment(Integer driverId, BigDecimal amount) throws Exception {
        if (amount == null || amount.compareTo(new BigDecimal("10000")) < 0) {
            throw new IllegalArgumentException("Số tiền nạp tối thiểu là 10,000đ.");
        }
        Driver driver = driverRepo.findById(driverId).orElseThrow(() -> new RuntimeException("Không tìm thấy tài xế"));

        // 1. Tạo giao dịch 'pending' trong DB
        Payment payment = Payment.builder()
                .driver(driver).amount(amount).status("pending").method("QR_CODE")
                .paidAt(Instant.now()).currency("VND").build();
        Payment savedPayment = paymentRepo.save(payment);

        // 2. Gọi gateway service để tạo mã QR
        return paymentGatewayService.createQrOrder(amount, savedPayment.getPaymentId().toString());
    }


    private PaymentHistoryResponse mapPaymentToResponse(Payment payment) {
        return PaymentHistoryResponse.builder()
                .date(DATE_FORMATTER.format(payment.getPaidAt()))
                .time(TIME_FORMATTER.format(payment.getPaidAt()))
                .formattedAmount("+" + CURRENCY_FORMATTER.format(payment.getAmount()))
                .status(payment.getStatus().toUpperCase())
                .build();
    }
}