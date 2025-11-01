package evswap.swp391to4.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.dto.PayOsWebhookRequest;
import evswap.swp391to4.dto.PayOsWebhookResponse;
import evswap.swp391to4.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Webhook endpoint để nhận callback từ PayOS
     * Endpoint này không yêu cầu authentication vì PayOS sẽ gọi trực tiếp
     */
    @PostMapping("/webhook")
    public ResponseEntity<PayOsWebhookResponse> handleWebhook(@RequestBody PayOsWebhookRequest request) {
        log.info("Received PayOS webhook: orderCode={}, status={}", 
            request.getData() != null ? request.getData().getOrderCode() : "null",
            request.getData() != null ? request.getData().getStatus() : "null");

        try {
            boolean processed = paymentService.handlePayOsWebhook(request);
            
            if (processed) {
                PayOsWebhookResponse response = PayOsWebhookResponse.builder()
                        .code(0)
                        .desc("success")
                        .data(PayOsWebhookResponse.PayOsData.builder()
                                .orderCode(request.getData().getOrderCode())
                                .providerTransactionCode("SUCCESS")
                                .build())
                        .build();
                return ResponseEntity.ok(response);
            } else {
                PayOsWebhookResponse response = PayOsWebhookResponse.builder()
                        .code(1)
                        .desc("Payment not found or invalid")
                        .build();
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            log.error("Error processing PayOS webhook", e);
            PayOsWebhookResponse response = PayOsWebhookResponse.builder()
                    .code(-1)
                    .desc("Internal server error")
                    .build();
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Test endpoint để kiểm tra webhook (chỉ dùng trong development)
     */
    @GetMapping("/webhook/test")
    public ResponseEntity<Map<String, Object>> testWebhook() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Webhook endpoint is ready");
        response.put("url", "/api/payment/webhook");
        response.put("method", "POST");
        return ResponseEntity.ok(response);
    }

    /**
     * API để lấy thông tin payment
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPayment(
            @PathVariable Integer paymentId,
            HttpSession session) {
        
        evswap.swp391to4.entity.Driver driver = (evswap.swp391to4.entity.Driver) session.getAttribute("loggedInDriver");
        
        if (driver == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            evswap.swp391to4.entity.Payment payment = paymentService.getPaymentByIdAndDriver(paymentId, driver);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentId", payment.getPaymentId());
            response.put("amount", payment.getAmount());
            response.put("status", payment.getStatus());
            response.put("method", payment.getMethod());
            response.put("providerTxnId", payment.getProviderTxnId());
            response.put("paidAt", payment.getPaidAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting payment", e);
            return ResponseEntity.status(404).build();
        }
    }

    /**
     * Chủ động đồng bộ trạng thái thanh toán PayOS khi webhook chậm.
     */
    @PostMapping("/{paymentId}/reconcile")
    public ResponseEntity<Map<String, Object>> reconcile(@PathVariable Integer paymentId, HttpSession session) {
        evswap.swp391to4.entity.Driver driver = (evswap.swp391to4.entity.Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return ResponseEntity.status(401).build();
        }

        evswap.swp391to4.entity.Payment payment = paymentService.reconcilePayOsPaymentById(paymentId);
        Map<String, Object> response = new HashMap<>();
        response.put("paymentId", payment.getPaymentId());
        response.put("status", payment.getStatus());
        return ResponseEntity.ok(response);
    }
}

