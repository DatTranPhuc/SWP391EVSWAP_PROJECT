package evswap.swp391to4.service;

import evswap.swp391to4.dto.AccountViewResponse;
import evswap.swp391to4.dto.PaymentHistoryResponse;
import evswap.swp391to4.dto.QrCodeResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Import Slf4j
import org.springframework.beans.factory.annotation.Qualifier;
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
@Slf4j // Dùng Slf4j của Lombok để tự tạo biến 'log'
public class AccountService {

    private final DriverRepository driverRepo;
    private final PaymentRepository paymentRepo;
    // Inject đúng bean service của VNPay
    @Qualifier("vnpayPaymentGateway")
    private final PaymentGatewayService paymentGatewayService;

    // Định dạng tiền tệ và ngày giờ
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    /**
     * Lấy thông tin chi tiết tài khoản để hiển thị.
     */
    public AccountViewResponse getAccountDetails(Integer driverId) {
        log.debug("Fetching account details for driver ID: {}", driverId);
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài xế với ID: " + driverId));

        // Đảm bảo Driver entity có trường balance và @Data
        return AccountViewResponse.builder()
                .driverName(driver.getFullName())
                .email(driver.getEmail())
                .formattedBalance(CURRENCY_FORMATTER.format(driver.getBalance()))
                .build();
    }

    /**
     * Lấy lịch sử nạp tiền của một tài xế.
     */
    public List<PaymentHistoryResponse> getPaymentHistory(Integer driverId) {
        log.debug("Fetching payment history for driver ID: {}", driverId);
        return paymentRepo.findByDriver_DriverIdOrderByPaidAtDesc(driverId).stream()
                .map(this::mapPaymentToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Xử lý logic cộng tiền vào tài khoản (thường được gọi từ webhook sau khi xác thực).
     */
    @Transactional
    public void addFunds(Integer driverId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Attempted to add non-positive funds ({}) for driverId: {}", amount, driverId);
            // Không nên ném exception ở đây để webhook không bị lỗi lặp lại
            // Cần có cơ chế log và cảnh báo riêng
            return;
        }

        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> {
                    log.error("Cannot add funds: Driver not found with ID: {}", driverId);
                    return new RuntimeException("Không tìm thấy tài xế để cộng tiền.");
                });

        BigDecimal oldBalance = driver.getBalance();
        driver.setBalance(oldBalance.add(amount));
        driverRepo.save(driver);
        log.info("Successfully added funds for driver ID: {}. Amount: {}, Old Balance: {}, New Balance: {}",
                driverId, amount, oldBalance, driver.getBalance());

        // Việc ghi lại giao dịch "succeed" nên được thực hiện trong logic xử lý webhook
        // sau khi đã xác nhận giao dịch gốc thành công.
    }

    /**
     * Khởi tạo một giao dịch nạp tiền qua QR code (gọi đến cổng thanh toán VNPay).
     * @return Một đối tượng chứa URL thanh toán của VNPay.
     */
    @Transactional
    public QrCodeResponse initiateQrPayment(Integer driverId, BigDecimal amount) throws Exception {
        if (amount == null || amount.compareTo(new BigDecimal("10000")) < 0) { // Số tiền tối thiểu
            throw new IllegalArgumentException("Số tiền nạp tối thiểu là 10,000đ.");
        }
        Driver driver = driverRepo.findById(driverId).orElseThrow(() -> new RuntimeException("Không tìm thấy tài xế"));

        // 1. Tạo một giao dịch 'Payment' trong hệ thống với trạng thái "pending"
        Payment payment = Payment.builder()
                .driver(driver)
                .amount(amount)
                .status("pending") // Trạng thái chờ thanh toán
                .method("QR_CODE") // Phương thức
                .paidAt(Instant.now()) // Thời điểm tạo yêu cầu
                .currency("VND")
                .build();
        Payment savedPayment = paymentRepo.save(payment);
        log.info("Created pending payment record with ID: {} for driver ID: {}", savedPayment.getPaymentId(), driverId);

        // 2. Gọi service của cổng thanh toán (VNPay) để tạo URL thanh toán
        QrCodeResponse response = paymentGatewayService.createQrOrder(amount, savedPayment.getPaymentId().toString());
        log.info("Successfully obtained VNPay payment URL for payment ID: {}", savedPayment.getPaymentId());
        return response;
    }

    /**
     * Phương thức private để chuyển đổi từ Payment Entity sang PaymentHistoryResponse DTO.
     */
    private PaymentHistoryResponse mapPaymentToResponse(Payment payment) {
        String dateStr = payment.getPaidAt() != null ? DATE_FORMATTER.format(payment.getPaidAt()) : "N/A";
        String timeStr = payment.getPaidAt() != null ? TIME_FORMATTER.format(payment.getPaidAt()) : "N/A";
        String statusStr = payment.getStatus() != null ? payment.getStatus().toUpperCase() : "UNKNOWN";

        // Thêm lại dòng return bị thiếu
        return PaymentHistoryResponse.builder()
                .date(dateStr)
                .time(timeStr)
                .formattedAmount("+" + CURRENCY_FORMATTER.format(payment.getAmount()))
                .status(statusStr)
                .build(); // <-- Dòng return
    }
}