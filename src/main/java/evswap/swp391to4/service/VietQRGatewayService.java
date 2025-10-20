package evswap.swp391to4.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import evswap.swp391to4.dto.QrCodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID; // Import thêm UUID

@Service
@RequiredArgsConstructor
public class VietQRGatewayService implements PaymentGatewayService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Đọc cấu hình của PayOS
    @Value("${vietqr.api.url}") // URL mới: https://api.payos.vn/v2/payment-requests
    private String apiUrl;
    @Value("${vietqr.api.client-id}")
    private String clientId;
    @Value("${vietqr.api.api-key}")
    private String apiKey;

    // ----- BẠN CÓ THỂ GIỮ NGUYÊN THÔNG TIN TÀI KHOẢN ĐỂ THAM KHẢO -----
    private static final String BANK_ACCOUNT_NO = "0975377747";
    private static final String ACCOUNT_NAME = "DAO THIEN THANH"; // Tên không dấu
    private static final int BANK_ACQ_ID = 970422; // Mã của MB Bank
    // --------------------------------------------------------------------

    // ----- CẦN THÊM CÁC URL ĐỂ PAYOS CHUYỂN HƯỚNG SAU KHI THANH TOÁN -----
    private static final String RETURN_URL = "http://localhost:8080/payment/success"; // URL khi thành công
    private static final String CANCEL_URL = "http://localhost:8080/payment/cancel";  // URL khi hủy
    // --------------------------------------------------------------------


    @Override
    public QrCodeResponse createQrOrder(BigDecimal amount, String internalTxnId) throws Exception {
        // [DEBUG] In ra key để kiểm tra
        System.out.println("--- DEBUG (PayOS): Client ID: [" + clientId + "]");
        System.out.println("--- DEBUG (PayOS): API Key: [" + apiKey + "]");

        HttpHeaders headers = new HttpHeaders();
        // PayOS dùng header x-client-id và x-api-key
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Tạo một mã đơn hàng duy nhất cho PayOS (họ yêu cầu)
        // Dùng mã giao dịch nội bộ kết hợp UUID để đảm bảo duy nhất
        long orderCode = Long.parseLong(internalTxnId + System.currentTimeMillis() % 1000); // Ví dụ đơn giản

        // [ĐÃ SỬA] Tạo request body theo yêu cầu của PayOS /v2/payment-requests
        Map<String, Object> body = new HashMap<>();
        body.put("orderCode", orderCode); // Mã đơn hàng duy nhất
        body.put("amount", amount.intValue());
        body.put("description", "EV SWAP nap tien " + internalTxnId);
        body.put("cancelUrl", CANCEL_URL); // URL khi người dùng hủy
        body.put("returnUrl", RETURN_URL); // URL khi thanh toán thành công
        // body.put("buyerName", "Optional Name"); // Tên người mua (tùy chọn)
        // body.put("buyerEmail", "Optional Email"); // Email người mua (tùy chọn)

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String code = root.path("code").asText(); // Code trả về của PayOS (00 là thành công)

                if ("00".equals(code)) {
                    // [ĐÃ SỬA] Lấy mã QR từ trường 'qrCode' (base64) theo tài liệu PayOS
                    String qrCodeBase64 = root.path("data").path("qrCode").asText();
                    String checkoutUrl = root.path("data").path("checkoutUrl").asText(); // Link thanh toán

                    if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
                        // Trả về QR code dưới dạng Data URL (base64)
                        return QrCodeResponse.builder()
                                .qrCodeUrl("data:image/png;base64," + qrCodeBase64) // Thêm tiền tố data url
                                .deepLink(checkoutUrl) // Có thể dùng link thanh toán làm deep link
                                .build();
                    } else {
                        throw new RuntimeException("Lỗi từ PayOS API: Không tìm thấy qrCode trong phản hồi.");
                    }
                } else {
                    String desc = root.path("desc").asText("Lỗi không xác định từ PayOS.");
                    throw new RuntimeException("Lỗi từ PayOS API: " + desc);
                }
            } else {
                throw new RuntimeException("Lỗi khi gọi PayOS API, status code: " + response.getStatusCode() + ", body: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo mã QR: " + e.getMessage());
        }
    }

    @Override
    public boolean verifySignature(String signature, String requestBody) {
        // TODO: PayOS sử dụng checksum để xác thực webhook, cần đọc tài liệu và triển khai
        return true; // Tạm thời bỏ qua
    }
}