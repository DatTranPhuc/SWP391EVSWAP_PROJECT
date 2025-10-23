package evswap.swp391to4.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@Getter // Lombok tự tạo các phương thức getter
public class VNPayConfig {

    @Value("${vnpay.config.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.config.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.config.paymentUrl}")
    private String paymentUrl; // URL thanh toán chính

    // Đọc URL trả về từ file application.properties
    @Value("${vnpay.config.returnUrl}")
    private String returnUrl;

    /**
     * Hàm tạo checksum HMAC SHA512 theo yêu cầu của tài liệu VNPay.
     * @param key Chuỗi bí mật (vnp_HashSecret).
     * @param data Chuỗi dữ liệu cần hash (các tham số đã được sắp xếp và nối lại).
     * @return Chuỗi checksum đã được mã hóa dưới dạng hex.
     */
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("Key hoặc data để tính HMAC không được null");
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            // Chuyển đổi mảng byte thành chuỗi hex
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception e) {
            // Trong ứng dụng thực tế nên ghi log lỗi chi tiết hơn
            System.err.println("Lỗi khi tạo HMACSHA512: " + e.getMessage());
            throw new RuntimeException("Lỗi khi tạo HMACSHA512", e);
        }
    }
}