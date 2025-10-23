package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.repository.PaymentRepository;
import evswap.swp391to4.service.AccountService;
import evswap.swp391to4.service.PaymentGatewayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity; // Không dùng ResponseEntity nữa
import org.springframework.stereotype.Controller; // Dùng @Controller
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller // Sử dụng @Controller cho cả hai phương thức
@RequestMapping("/payment") // Đường dẫn chung
@RequiredArgsConstructor
public class PaymentController {

    private final AccountService accountService;
    private final PaymentRepository paymentRepository;
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Qualifier("vnpayPaymentGateway")
    private final PaymentGatewayService paymentGatewayService;

    /**
     * Endpoint xử lý IPN từ VNPay (API).
     * Dùng @ResponseBody để trả về String JSON trực tiếp.
     */
    @GetMapping("/webhook/vnpay_ipn")
    @ResponseBody // Quan trọng: Đảm bảo trả về String, không tìm view
    @Transactional
    public String handleVNPayIPN(HttpServletRequest request) {
        log.info("Received VNPay IPN callback.");
        Map<String, String> fields = new HashMap<>();
        // Đọc tham số từ request
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                fields.put(fieldName, fieldValue);
            }
        }
        log.debug("VNPay IPN Params received: {}", fields);

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHash")) fields.remove("vnp_SecureHash");
        else {
            log.error("IPN Error: Missing vnp_SecureHash.");
            return "{\"RspCode\":\"97\",\"Message\":\"Invalid Checksum\"}";
        }

        // --- BƯỚC 1: Xác thực chữ ký ---
        boolean isValidSignature = paymentGatewayService.verifySignature(vnp_SecureHash, fields);
        if (!isValidSignature) {
            log.error("Invalid VNPay IPN signature. Params: {}", fields);
            return "{\"RspCode\":\"97\",\"Message\":\"Invalid Checksum\"}";
        }
        log.info("VNPay IPN signature verified successfully.");

        // --- BƯỚC 2: Lấy tham số ---
        String vnp_ResponseCode = fields.get("vnp_ResponseCode");
        String vnp_TxnRef = fields.get("vnp_TxnRef"); // Format: paymentId_timestamp
        String vnp_AmountStr = fields.get("vnp_Amount");
        String vnp_TransactionNo = fields.get("vnp_TransactionNo"); // Mã GD VNPay
        log.info("Processing IPN for TxnRef: {}, ResponseCode: {}, Amount: {}", vnp_TxnRef, vnp_ResponseCode, vnp_AmountStr);

        // --- BƯỚC 3: Xử lý nghiệp vụ ---
        String responseCode = "99"; String message = "Unknown error";
        try {
            // 3.1: Parse Payment ID
            if (vnp_TxnRef == null || !vnp_TxnRef.contains("_")) { /* ... xử lý lỗi ... */ }
            String paymentIdStr = vnp_TxnRef.substring(0, vnp_TxnRef.indexOf("_"));
            Integer paymentId = Integer.parseInt(paymentIdStr);

            // 3.2: Tìm giao dịch Payment
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) { /* ... xử lý lỗi ... */ }
            Payment payment = paymentOpt.get();

            // 3.3: Kiểm tra trạng thái
            if (!"pending".equalsIgnoreCase(payment.getStatus())) { /* ... xử lý lỗi (trả về 02) ... */ }

            // 3.4: Kiểm tra số tiền
            BigDecimal vnpAmount = new BigDecimal(vnp_AmountStr).divide(new BigDecimal(100));
            if (vnpAmount.compareTo(payment.getAmount()) != 0) { /* ... xử lý lỗi (trả về 04) ... */ }

            // 3.5: Xử lý dựa trên mã phản hồi
            if ("00".equals(vnp_ResponseCode)) {
                // THÀNH CÔNG
                payment.setStatus("succeed");
                payment.setProviderTxnId(vnp_TransactionNo);
                paymentRepository.save(payment);
                accountService.addFunds(payment.getDriver().getDriverId(), payment.getAmount());
                log.info("IPN Success: Processed Payment ID: {}, TxnRef: {}", paymentId, vnp_TxnRef);
                responseCode = "00"; message = "Confirm Success";
            } else {
                // THẤT BẠI
                payment.setStatus("failed");
                payment.setProviderTxnId(vnp_TransactionNo);
                paymentRepository.save(payment);
                log.warn("IPN Failed: Payment ID: {}, TxnRef: {}, ResponseCode: {}", paymentId, vnp_TxnRef, vnp_ResponseCode);
                responseCode = "00"; message = "Confirm Success"; // Vẫn trả về 00 cho VNPay
            }
            return "{\"RspCode\":\"" + responseCode + "\",\"Message\":\"" + message + "\"}";

        } catch (NumberFormatException e) { /* ... xử lý lỗi ... */ }
        catch (Exception e) { /* ... xử lý lỗi ... */ }
        // Các khối catch giữ nguyên như code trước
        return "{\"RspCode\":\"99\",\"Message\":\"Unknown error\"}"; // Trả về lỗi chung nếu có exception
    }

    /**
     * Endpoint xử lý Return URL từ VNPay (MVC).
     * Chuyển hướng người dùng về trang tài khoản với thông báo.
     */
    @GetMapping("/vnpay_return")
    public String handleVNPayReturn(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.info("Received VNPay Return callback.");
        Map<String, String> fields = new HashMap<>();
        // ... code đọc tham số ...
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) { /* ... */ }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHash")) fields.remove("vnp_SecureHash");
        else { /* Xử lý lỗi thiếu hash */ }

        // --- BƯỚC 1: Xác thực chữ ký ---
        boolean isValidSignature = paymentGatewayService.verifySignature(vnp_SecureHash, fields);
        if (!isValidSignature) {
            log.error("Invalid VNPay return signature.");
            redirectAttributes.addFlashAttribute("accountError", "Lỗi: Dữ liệu trả về từ VNPay không hợp lệ.");
            return "redirect:/account";
        }
        log.info("VNPay return signature verified successfully.");

        // --- BƯỚC 2 & 3: Kiểm tra kết quả và hiển thị thông báo ---
        String vnp_ResponseCode = fields.get("vnp_ResponseCode");
        log.info("Return Details: TxnRef={}, ResponseCode={}", fields.get("vnp_TxnRef"), vnp_ResponseCode);

        if ("00".equals(vnp_ResponseCode)) {
            redirectAttributes.addFlashAttribute("accountSuccess", "Giao dịch VNPay thành công! Số dư sẽ được cập nhật sau ít phút.");
        } else {
            redirectAttributes.addFlashAttribute("accountError", "Giao dịch VNPay thất bại hoặc bị hủy (Mã lỗi: " + vnp_ResponseCode + ").");
        }
        return "redirect:/account";
    }
}