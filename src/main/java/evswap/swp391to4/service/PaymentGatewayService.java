package evswap.swp391to4.service;

import evswap.swp391to4.dto.QrCodeResponse;
import java.math.BigDecimal;
import java.util.Map; // Thêm import Map

/**
 * Interface định nghĩa các chức năng chung mà một cổng thanh toán cần cung cấp.
 * Giúp dễ dàng thay đổi hoặc thêm nhà cung cấp thanh toán khác trong tương lai.
 */
public interface PaymentGatewayService {

    /**
     * Tạo yêu cầu thanh toán (ví dụ: tạo QR code hoặc URL thanh toán).
     * @param amount Số tiền.
     * @param internalTxnId Mã giao dịch nội bộ duy nhất.
     * @return Dữ liệu cần thiết để hiển thị cho người dùng (URL, mã QR...).
     */
    QrCodeResponse createQrOrder(BigDecimal amount, String internalTxnId) throws Exception;

    /**
     * Xác thực chữ ký/checksum từ thông báo IPN hoặc Return URL của cổng thanh toán.
     * @param signature Chữ ký nhận được.
     * @param fields Dữ liệu nhận được (dưới dạng Map cho VNPay, có thể là String cho nhà cung cấp khác).
     * @return true nếu chữ ký hợp lệ.
     */
    boolean verifySignature(String signature, Map<String, String> fields);
}