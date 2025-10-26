package evswap.swp391to4.service;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final VnPayService vnPayService; // << TIÊM (INJECT) VnPayService

    /**
     * Hàm này được gọi ngay sau khi tạo Reservation.
     * Nó tạo ra một Payment "pending" và trả về URL thanh toán.
     */
    @Transactional
    public String createPaymentForReservation(HttpServletRequest httpReq, Reservation reservation, Driver driver)
            throws UnsupportedEncodingException {

        // 1. Quy định số tiền (ví dụ: 10,000 VND)
        BigDecimal amount = new BigDecimal("10000.00");

        // 2. Tạo một đối tượng Payment
        Payment payment = Payment.builder()
                .driver(driver)
                .reservation(reservation)
                .amount(amount)
                .method("ewallet") // Giả sử là VNPay
                .status("pending")
                .currency("VND")
                .build();

        Payment savedPayment = paymentRepo.save(payment);

        // 3. GỌI VNPayService THẬT
        // Tạo mã đơn hàng và thông tin
        String orderId = String.valueOf(savedPayment.getPaymentId());
        String orderInfo = "Thanh toan don dat lich " + reservation.getReservationId();

        // Trả về URL thanh toán do VnPayService tạo
        return vnPayService.createPaymentUrl(httpReq, amount.longValue(), orderInfo, orderId);
    }

    /**
     * Hàm này được gọi bởi VNPay (IPN hoặc Return) để xác nhận thanh toán.
     * Nó trả về một mã trạng thái để Controller biết.
     */
    @Transactional
    public String processVnPayResponse(Map<String, String[]> requestParams) {

        // Lấy các tham số cần thiết từ VNPay
        String vnp_TxnRef = requestParams.get("vnp_TxnRef")[0];
        String vnp_ResponseCode = requestParams.get("vnp_ResponseCode")[0];
        String vnp_TransactionNo = requestParams.get("vnp_TransactionNo")[0];

        Integer paymentId = Integer.parseInt(vnp_TxnRef);

        // 1. Tìm thanh toán trong DB
        Payment payment = paymentRepo.findById(paymentId)
                .orElse(null);

        if (payment == null) {
            return "PaymentNotFound"; // Lỗi: Không tìm thấy thanh toán
        }

        // 2. Chỉ xử lý nếu đang là "pending" (tránh xử lý lặp)
        if (!"pending".equals(payment.getStatus())) {
            return "OrderAlreadyConfirmed"; // Lỗi: Đơn đã xử lý
        }

        // 3. Cập nhật trạng thái dựa trên mã trả về của VNPay
        if ("00".equals(vnp_ResponseCode)) {
            // Thanh toán THÀNH CÔNG
            payment.setStatus("succeed");
            payment.setPaidAt(Instant.now());
            payment.setProviderTxnId(vnp_TransactionNo);

            // Cập nhật Reservation liên quan
            Reservation reservation = payment.getReservation();
            if (reservation != null) {
                reservation.setStatus("confirmed"); // Đổi từ "pending" -> "confirmed"
            }

            // TODO: Gửi notification cho user (nếu cần)
            // notificationService.sendPaymentSuccess(driver, payment);

        } else {
            // Thanh toán THẤT BẠI
            payment.setStatus("failed");
        }

        paymentRepo.save(payment);
        return "OK"; // Trả về OK
    }
}