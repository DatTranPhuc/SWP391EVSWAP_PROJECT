package evswap.swp391to4.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PayOsUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Tạo chữ ký HMAC SHA256 cho webhook verification
     * 
     * @param data      Dữ liệu cần ký
     * @param secretKey Secret key từ PayOS
     * @return Chữ ký hex string
     */
    public static String createHmacSignature(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error creating HMAC signature", e);
            throw new RuntimeException("Failed to create HMAC signature", e);
        }
    }

    /**
     * Verify chữ ký webhook từ PayOS
     * 
     * @param signature Chữ ký nhận được từ PayOS
     * @param data      Dữ liệu gốc
     * @param secretKey Secret key
     * @return true nếu chữ ký hợp lệ
     */
    public static boolean verifySignature(String signature, String data, String secretKey) {
        if (signature == null || data == null || secretKey == null) {
            return false;
        }
        
        String calculatedSignature = createHmacSignature(data, secretKey);
        return signature.equalsIgnoreCase(calculatedSignature);
    }

    /**
     * Convert byte array to hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Tạo data string để verify webhook signature từ PayOS
     * Format: code|desc|orderCode|amount|status
     */
    public static String createWebhookDataString(String code, String desc, Long orderCode, Long amount, String status) {
        return String.format("%s|%s|%d|%d|%s", code, desc, orderCode, amount, status);
    }

    /**
     * Build signature data string (with encoding) for payment request theo chuẩn PayOS (fields phải encodeURI)
     */
    public static String buildPaymentRequestSignString(
            long amount,
            String cancelUrl,
            String clientId,
            String description,
            long orderCode,
            String successUrl,
            String webhookUrl) {
        // KHÔNG encode URI, chỉ nối raw đúng docs PayOS
        return amount + "|" + cancelUrl + "|" + clientId + "|" + description + "|" + orderCode + "|" + successUrl + "|" + webhookUrl;
    }
}

