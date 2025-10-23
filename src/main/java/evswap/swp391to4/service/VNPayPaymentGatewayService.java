package evswap.swp391to4.service;

import evswap.swp391to4.config.VNPayConfig;
import evswap.swp391to4.dto.QrCodeResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*; // Import HttpStatus (nếu cần xử lý lỗi)
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate; // Import RestTemplate (nếu dùng)
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("vnpayPaymentGateway") // Tên bean cho VNPay
@Qualifier("vnpayPaymentGateway")
@RequiredArgsConstructor
public class VNPayPaymentGatewayService implements PaymentGatewayService {

    private final VNPayConfig vnPayConfig;
    private static final Logger log = LoggerFactory.getLogger(VNPayPaymentGatewayService.class);

    @Override
    public QrCodeResponse createQrOrder(BigDecimal amount, String internalTxnId) throws Exception {
        log.info("Creating VNPay payment URL for internalTxnId: {}, Amount: {}", internalTxnId, amount);

        // --- Các tham số cố định ---
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_CurrCode = "VND";
        String vnp_Locale = "vn";
        String orderType = "other"; // Mã loại hàng hóa

        // --- Các tham số động ---
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String vnp_ReturnUrl = vnPayConfig.getReturnUrl();
        long amountValue = amount.multiply(new BigDecimal(100)).longValue(); // VNPay yêu cầu * 100
        String vnp_TxnRef = internalTxnId.replaceAll("[^a-zA-Z0-9]", "") + "_" + System.currentTimeMillis();
        String vnp_OrderInfo = "Nap tien EV SWAP " + internalTxnId;
        String vnp_IpAddr = getClientIpAddress();
        log.debug("Client IP Address for VNPay: {}", vnp_IpAddr);

        // Lấy thời gian hiện tại và thời gian hết hạn theo múi giờ VN
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        cld.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút
        String vnp_ExpireDate = formatter.format(cld.getTime());

        // --- Tạo Map chứa các tham số ---
        Map<String, String> vnp_Params = new TreeMap<>(); // Sử dụng TreeMap để tự động sắp xếp key
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountValue));
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // --- Tạo chuỗi hashData và queryUrl ---
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = vnp_Params.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // Build hash data (encode UTF-8)
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                // Build query string (encode UTF-8)
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                // Thêm dấu '&' nếu không phải phần tử cuối
                if (iterator.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }
        String hashDataString = hashData.toString();
        String queryString = query.toString();

        // --- Tạo checksum và URL cuối cùng ---
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashDataString);
        queryString += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = vnPayConfig.getPaymentUrl() + "?" + queryString;

        // [ĐÃ THÊM] Log URL cuối cùng trước khi trả về
        log.info("Final Payment URL for QR: {}", paymentUrl);

        // Trả về URL thanh toán, frontend sẽ dùng URL này để tạo mã QR
        return QrCodeResponse.builder()
                .qrCodeUrl(paymentUrl)
                .deepLink(paymentUrl) // Link thanh toán cũng dùng làm deep link
                .build();
    }

    /**
     * Xác thực chữ ký IPN/Return URL của VNPay.
     */
    @Override
    public boolean verifySignature(String receivedSignature, Map<String, String> fields) {
        // ... (code hàm này giữ nguyên như phiên bản trước) ...
        if (receivedSignature == null || fields == null || fields.isEmpty()) {
            log.warn("Cannot verify VNPay signature: Missing signature or fields.");
            return false;
        }
        String secretKey = vnPayConfig.getHashSecret();
        if (secretKey == null || secretKey.isEmpty()) {
            log.error("Cannot verify VNPay signature: HashSecret is not configured.");
            return false;
        }
        Map<String, String> sortedFields = new TreeMap<>(fields);
        StringBuilder hashData = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = sortedFields.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            if ((fieldValue != null) && (!fieldValue.isEmpty()) &&
                    !fieldName.equals("vnp_SecureHash") && !fieldName.equals("vnp_SecureHashType")) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    log.error("Error encoding field value [{}] for signature verification", fieldName, e);
                    return false;
                }
                if (iterator.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        String hashDataString = hashData.toString();
        String calculatedSignature = VNPayConfig.hmacSHA512(secretKey, hashDataString);
        log.debug("Received Signature: [{}]", receivedSignature);
        log.debug("Calculated Signature: [{}]", calculatedSignature);
        log.debug("Hash Data String for verification: [{}]", hashDataString);
        boolean isValid = receivedSignature.equalsIgnoreCase(calculatedSignature);
        if (!isValid) {
            log.warn("VNPay signature mismatch!");
        } else {
            log.info("VNPay signature verification successful.");
        }
        return isValid;
    }

    // Hàm lấy IP người dùng (không đổi)
    private String getClientIpAddress() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }
            if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
            return ipAddress;
        } catch (IllegalStateException e) {
            log.warn("Could not get HttpServletRequest, possibly called outside of a request context. Returning default IP 127.0.0.1.");
            return "127.0.0.1";
        } catch (Exception e) {
            log.error("Unexpected error getting client IP address.", e);
            return "127.0.0.1";
        }
    }
}