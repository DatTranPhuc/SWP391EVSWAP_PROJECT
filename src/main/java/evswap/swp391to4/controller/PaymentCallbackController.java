package evswap.swp391to4.controller;

import evswap.swp391to4.service.VnPayService;
import evswap.swp391to4.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final PaymentService paymentService;
    private final VnPayService vnPayService;

    /**
     * Đây là trang VNPay chuyển hướng người dùng VỀ sau khi họ thanh toán
     */
    @GetMapping("/return")
    public String vnPayReturn(HttpServletRequest request, Model model) throws UnsupportedEncodingException {

        // 1. Kiểm tra chữ ký
        boolean isValidSignature = vnPayService.verifySignature(request);
        if (!isValidSignature) {
            model.addAttribute("paymentStatus", "failed");
            model.addAttribute("message", "Chữ ký không hợp lệ! Giao dịch có thể đã bị can thiệp.");
            return "payment-result"; // Tên file HTML hiển thị kết quả
        }

        // 2. Lấy các tham số VNPay trả về
        String responseCode = request.getParameter("vnp_ResponseCode");

        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            // Cập nhật DB (có thể đã được cập nhật bởi IPN, nhưng ta cứ làm cho chắc)
            paymentService.processVnPayResponse(request.getParameterMap());

            model.addAttribute("paymentStatus", "success");
            model.addAttribute("message", "Thanh toán của bạn đã được xử lý thành công!");
        } else {
            // Thanh toán thất bại
            model.addAttribute("paymentStatus", "failed");
            model.addAttribute("message", "Thanh toán thất bại. Vui lòng thử lại.");
        }

        return "payment-result"; // Tên file HTML hiển thị kết quả
    }

    /**
     * Đây là URL mà HỆ THỐNG VNPay bí mật gọi để xác nhận (IPN)
     * Đây là nơi quan trọng nhất để cập nhật DB
     */
    @GetMapping("/notify")
    public ResponseEntity<String> vnPayNotify(HttpServletRequest request) throws UnsupportedEncodingException {

        // 1. Kiểm tra chữ ký
        boolean isValidSignature = vnPayService.verifySignature(request);
        if (!isValidSignature) {
            // Trả về lỗi cho VNPay
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"RspCode\":\"97\",\"Message\":\"Invalid Checksum\"}");
        }

        // 2. Xử lý logic thanh toán
        String status = paymentService.processVnPayResponse(request.getParameterMap());

        // 3. Trả về kết quả cho VNPay
        if ("OK".equals(status)) {
            return ResponseEntity.ok("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
        } else {
            // Các lỗi khác do logic nghiệp vụ (đơn đã xử lý, không tìm thấy đơn)
            return ResponseEntity.ok("{\"RspCode\":\"01\",\"Message\":\"Order not found or already confirmed\"}");
        }
    }
}