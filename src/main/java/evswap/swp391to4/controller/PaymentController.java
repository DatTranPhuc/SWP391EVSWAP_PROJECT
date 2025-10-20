package evswap.swp391to4.controller;

import evswap.swp391to4.dto.DepositRequest;
import evswap.swp391to4.dto.QrCodeResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.AccountService;
import evswap.swp391to4.service.PaymentGatewayService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Dùng @RestController vì các phương thức này trả về dữ liệu JSON
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final AccountService accountService;
    private final PaymentGatewayService paymentGatewayService; // Thêm import này

    /**
     * API để người dùng yêu cầu tạo mã QR để nạp tiền.
     * Xử lý yêu cầu POST đến /api/payment/qr/create
     */
    @PostMapping("/qr/create")
    public ResponseEntity<?> createQrPayment(HttpSession session, @RequestBody DepositRequest request) {
        Driver loggedInDriver = (Driver) session.getAttribute("loggedInDriver");
        if (loggedInDriver == null) {
            // Trả về lỗi 401 Unauthorized nếu người dùng chưa đăng nhập
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để thực hiện chức năng này.");
        }

        try {
            // Gọi service để khởi tạo thanh toán và tạo mã QR
            QrCodeResponse qrCodeResponse = accountService.initiateQrPayment(loggedInDriver.getDriverId(), request.getAmount());
            return ResponseEntity.ok(qrCodeResponse);
        } catch (Exception e) {
            // Trả về lỗi 400 Bad Request nếu có lỗi xảy ra (ví dụ: số tiền không hợp lệ)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API để CỔNG THANH TOÁN gửi thông báo kết quả giao dịch về (Webhook/IPN).
     * URL này sẽ được gọi bởi hệ thống của Momo/VietQR..., không phải bởi người dùng.
     */
    @PostMapping("/webhook/vietqr") // Ví dụ với VietQR
    public ResponseEntity<?> handleVietQRWebhook(@RequestBody String body) {
        System.out.println("Received VietQR Webhook: " + body);

        // TODO: Xử lý logic webhook ở đây
        // 1. Xác thực thông báo đến từ VietQR (nếu họ có cung cấp cơ chế).
        // 2. Phân tích 'body' (JSON) để lấy mã giao dịch và trạng thái.
        // 3. Nếu giao dịch thành công ("succeed"), tìm 'Payment' trong DB, cập nhật trạng thái,
        //    và gọi service để cộng tiền vào ví người dùng.

        // Luôn trả về HTTP 200 OK để báo cho cổng thanh toán biết là đã nhận được.
        return ResponseEntity.ok().build();
    }
}