package evswap.swp391to4.service;

import evswap.swp391to4.dto.QrCodeResponse;
import java.math.BigDecimal;

public interface PaymentGatewayService {

    /**
     * Gửi yêu cầu tạo một đơn hàng thanh toán qua QR code.
     * @param amount Số tiền cần thanh toán.
     * @param internalTxnId Mã giao dịch nội bộ của hệ thống chúng ta (ví dụ: paymentId).
     * @return Một đối tượng chứa thông tin QR code.
     */
    QrCodeResponse createQrOrder(BigDecimal amount, String internalTxnId) throws Exception;

    /**
     * Xác thực chữ ký từ webhook/IPN của cổng thanh toán.
     * @param signature Chữ ký do cổng thanh toán gửi đến.
     * @param requestBody Nội dung yêu cầu mà cổng thanh toán gửi.
     * @return true nếu chữ ký hợp lệ, false nếu không.
     */
    boolean verifySignature(String signature, String requestBody);
}