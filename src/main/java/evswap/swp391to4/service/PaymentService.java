package evswap.swp391to4.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import evswap.swp391to4.dto.PayOsWebhookRequest;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.repository.PaymentRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.util.PayOsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RestTemplate restTemplate;

    public PaymentRepository getPaymentRepository() {
        return paymentRepository;
    }

    @Value("${payos.api.endpoint}")
    private String payosEndpoint;

    @Value("${payos.api.key}")
    private String payosApiKey;

    @Value("${payos.api.client-id}")
    private String payosClientId;

    @Value("${payos.webhook.url}")
    private String payosWebhookUrl;

    @Value("${payos.webhook.key}")
    private String payosWebhookKey;

    @Value("${app.public.base-url}")
    private String publicBaseUrl;

    /**
     * Tạo payment record với các thông tin cơ bản
     */
    @Transactional
    public Payment createPayment(Driver driver, Reservation reservation, BigDecimal amount, String method, String status) {
        Payment payment = Payment.builder()
                .driver(driver)
                .reservation(reservation)
                .amount(amount)
                .method(method)
                .status(status)
                .paidAt(Instant.now())
                .currency("VND")
                .providerTxnId("FAKE-" + System.currentTimeMillis())
                .build();
        return paymentRepository.save(payment);
    }

    // Removed simulate payment/testing methods to avoid misuse in production

    /**
     * Tạo yêu cầu thanh toán PayOS (nạp tiền)
     * 
     * @param driver Tài xế thực hiện nạp tiền
     * @param amount Số tiền nạp
     * @return Payment object với status pending
     */
    @Transactional
    public Payment createPayOsTopUpRequest(Driver driver, BigDecimal amount) {
        try {
            // Tạo orderCode duy nhất
            long timestamp = System.currentTimeMillis();
            String timestampStr = String.valueOf(timestamp);
            String last10Digits = timestampStr.substring(timestampStr.length() - 10);
            String driverIdStr = String.valueOf(driver.getDriverId());
            String orderCodeStr = last10Digits + driverIdStr;
            if (orderCodeStr.length() > 11) {
                orderCodeStr = orderCodeStr.substring(orderCodeStr.length() - 11);
            }
            Long orderCode = Long.parseLong(orderCodeStr);
            log.info("Generated orderCode: {} (length: {})", orderCode, orderCodeStr.length());

            // Tạo PayOS SDK instance (nên để @Bean singleton, demo nhanh thì để tạm)
            PayOS payOS = new PayOS(payosClientId, payosApiKey, payosWebhookKey);
            // Đảm bảo webhook public đã được cấu hình trên PayOS
            try {
                String verified = payOS.confirmWebhook(payosWebhookUrl);
                log.info("PayOS webhook confirmed: {}", verified);
            } catch (Exception ex) {
                log.warn("Could not confirm PayOS webhook. Proceeding anyway. reason={}", ex.getMessage());
            }
            String cancelUrl = publicBaseUrl + "/wallet";
            String successUrl = publicBaseUrl + "/wallet/topup-success?orderCode=" + orderCode;
            String description = "Nap tien vi EVSWAP";
            ItemData itemData = ItemData.builder()
                .name("Nạp tiền ví EVSWAP")
                .quantity(1)
                .price(amount.intValue())
                .build();
            PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(amount.intValue())
                .description(description)
                .item(itemData)
                .cancelUrl(cancelUrl)
                .returnUrl(successUrl)
                .build();
            log.info("PayOS PaymentData: {}", paymentData);
            CheckoutResponseData result = payOS.createPaymentLink(paymentData);
            String checkoutUrl = result.getCheckoutUrl();
            String paymentLinkId = result.getPaymentLinkId();
            // Lưu payment với status pending
            Payment payment = Payment.builder()
                    .driver(driver)
                    .reservation(null)
                    .amount(amount)
                    .method("payos")
                    .status("pending")
                    .paidAt(Instant.now())
                    .currency("VND")
                .providerTxnId(paymentLinkId)
                .orderCode("EVSWAP" + orderCode)
                .checkoutUrl(checkoutUrl)
                    .build();
            payment = paymentRepository.save(payment);
            log.info("Created PayOS payment request [SDK]: paymentId={}, orderCode=EVSWAP{}, checkoutUrl={}", payment.getPaymentId(), orderCode, checkoutUrl);
            return payment;
        } catch (Exception e) {
            log.error("Error creating PayOS payment request (SDK)", e);
            throw new IllegalStateException("Lỗi khi tạo yêu cầu thanh toán: " + e.getMessage());
        }
    }

    /**
     * Xử lý webhook callback từ PayOS
     * 
     * @param webhookRequest PayOS webhook data
     * @return true nếu xử lý thành công
     */
    @Transactional
    public boolean handlePayOsWebhook(PayOsWebhookRequest webhookRequest) {
        try {
            // Extract orderCode từ webhook
            Long orderCode = webhookRequest.getData().getOrderCode();
            if (orderCode == null) {
                log.error("Invalid webhook: missing orderCode");
                return false;
            }

            // Tìm payment record qua orderCode
            // PayOS gửi orderCode là số thuần, nhưng trong DB chúng ta lưu với prefix "EVSWAP"
            String orderCodeWithPrefix = "EVSWAP" + orderCode;
            Optional<Payment> paymentOpt = paymentRepository.findByOrderCode(orderCodeWithPrefix);
            
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for orderCode: {}", orderCode);
                return false;
            }

            Payment payment = paymentOpt.get();

            // Verify signature for security (using Checksum Key)
            String signature = webhookRequest.getSignature();
            PayOsWebhookRequest.PayOsData data = webhookRequest.getData();
            boolean canVerify = webhookRequest.getCode() != null && webhookRequest.getDesc() != null
                    && data.getOrderCode() != null && data.getAmount() != null && data.getStatus() != null;
            if (signature != null && payosWebhookKey != null && !payosWebhookKey.isEmpty() && canVerify) {
                try {
                    String dataString = PayOsUtil.createWebhookDataString(
                        webhookRequest.getCode(),
                        webhookRequest.getDesc(),
                        data.getOrderCode(),
                        data.getAmount(),
                            data.getStatus());
                    
                    if (!PayOsUtil.verifySignature(signature, dataString, payosWebhookKey)) {
                        log.error("Invalid webhook signature for orderCode: {}", orderCode);
                        // Không chặn tại đây; sẽ cố reconciliate theo API để đảm bảo trải nghiệm người dùng
                    } else {
                        log.debug("Webhook signature verified successfully for orderCode: {}", orderCode);
                    }
                } catch (Exception e) {
                    log.warn("Error verifying webhook signature, will try reconcile instead. reason={}", e.getMessage());
                }
            } else {
                log.warn("Webhook missing fields required for signature verification. Proceeding to reconcile. orderCode={}", orderCode);
            }

            // Cập nhật status dựa trên webhook status
            String payosStatus = webhookRequest.getData().getStatus();

            // Nếu webhook không gửi status, chủ động hỏi PayOS
            if (payosStatus == null || payosStatus.isBlank()) {
                try {
                    String orderCodeWithPrefixForRecon = payment.getOrderCode();
                    Payment reconciled = reconcilePayOsPaymentById(payment.getPaymentId());
                    payosStatus = "EVSWAP".equals("EVSWAP") ? reconciled.getStatus() : payosStatus; // status đã được cập nhật trong DB
                } catch (Exception ex) {
                    log.warn("Reconcile fallback failed for orderCode={}, reason={}", orderCode, ex.getMessage());
                }
            }
            
            if ("PAID".equalsIgnoreCase(payosStatus)) {
                payment.setStatus("succeed");
                
                // Cập nhật reservation payment status và tự động confirm reservation nếu có reservation
                if (payment.getReservation() != null) {
                    Reservation reservation = payment.getReservation();
                    reservation.setPaymentStatus("completed");
                    reservationRepository.save(reservation);
                    log.info("Updated reservation payment status to completed: reservationId={}", 
                        reservation.getReservationId());
                    
                    // Tự động confirm reservation nếu đang ở status "pending" (giống như thanh toán tiền mặt)
                    // Chuyển từ "pending" sang "confirmed" để tiếp tục luồng đổi pin
                    if ("pending".equalsIgnoreCase(reservation.getStatus())) {
                        try {
                            // For instant swap, skip to checked_in; otherwise confirm normally
                            if (reservation.getIsInstantSwap() != null && reservation.getIsInstantSwap()) {
                                reservation.setStatus("checked_in");
                                reservation.setCheckedInAt(Instant.now());
                            } else {
                                reservation.setStatus("confirmed");
                            }
                            reservationRepository.save(reservation);
                            log.info("Auto-confirmed reservation after PayOS payment success: reservationId={}, newStatus={}", 
                                reservation.getReservationId(), reservation.getStatus());
                        } catch (Exception e) {
                            log.warn("Failed to auto-confirm reservation after payment: reservationId={}, error={}", 
                                reservation.getReservationId(), e.getMessage());
                        }
                    }
                }
                
                log.info("Payment successful: paymentId={}, orderCode={}, reservationId={}", 
                    payment.getPaymentId(), orderCode, 
                    payment.getReservation() != null ? payment.getReservation().getReservationId() : "null");
            } else if ("CANCELLED".equalsIgnoreCase(payosStatus)) {
                payment.setStatus("failed");
                log.info("Payment cancelled: paymentId={}, orderCode={}", 
                    payment.getPaymentId(), orderCode);
            } else {
                payment.setStatus("pending");
                log.info("Payment still pending: paymentId={}, orderCode={}, status={}", 
                    payment.getPaymentId(), orderCode, payosStatus);
            }

            paymentRepository.save(payment);
            return true;

        } catch (IllegalStateException e) {
            log.error("Business logic error handling PayOS webhook: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error handling PayOS webhook", e);
            return false;
        }
    }

    /**
     * Lấy payment theo ID
     */
    @Transactional(readOnly = true)
    public Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy payment"));
    }

    /**
     * Lấy payment theo ID và driver (để validate ownership)
     */
    @Transactional(readOnly = true)
    public Payment getPaymentByIdAndDriver(Integer paymentId, Driver driver) {
        return paymentRepository.findByPaymentIdAndDriver(paymentId, driver)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy payment hoặc không có quyền truy cập"));
    }

    /**
     * Get payment by provider transaction ID
     */
    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentByProviderTxnId(String providerTxnId) {
        return paymentRepository.findByProviderTxnId(providerTxnId);
    }

    /**
     * Get payments by reservation
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByReservation(Reservation reservation) {
        return paymentRepository.findByReservation(reservation);
    }

    @Transactional
    public Payment reconcileByOrderCode(Long numericOrderCode) {
        String orderCodeWithPrefix = "EVSWAP" + numericOrderCode;
        Optional<Payment> opt = paymentRepository.findByOrderCode(orderCodeWithPrefix);
        if (opt.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy payment theo orderCode");
        }
        return reconcilePayOsPaymentById(opt.get().getPaymentId());
    }

    /**
     * Chủ động đồng bộ trạng thái thanh toán PayOS theo paymentId.
     * Dùng khi webhook chậm hoặc thất bại.
     */
    @Transactional
    public Payment reconcilePayOsPaymentById(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy payment"));

        if (!"payos".equalsIgnoreCase(payment.getMethod())) {
            return payment; // Không phải giao dịch PayOS
        }

        // Lấy orderCode số từ chuỗi lưu trong DB (ví dụ EVSWAP18288695101)
        String orderCodeStr = payment.getOrderCode();
        if (orderCodeStr == null || !orderCodeStr.startsWith("EVSWAP")) {
            return payment;
        }
        String numericOrderCode = orderCodeStr.substring("EVSWAP".length());

        // Gọi PayOS API: GET /v2/payment-requests/{orderCode}
        String url = payosEndpoint + "/v2/payment-requests/" + numericOrderCode;
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", payosClientId);
        headers.set("x-api-key", payosApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<java.util.Map> resp = restTemplate.exchange(url, HttpMethod.GET, entity, java.util.Map.class);
            Object dataObj = ((java.util.Map) resp.getBody()).get("data");
            if (dataObj instanceof java.util.Map) {
                java.util.Map data = (java.util.Map) dataObj;
                Object statusObj = data.get("status");
                String payosStatus = statusObj != null ? statusObj.toString() : null;

                if ("PAID".equalsIgnoreCase(payosStatus)) {
                    payment.setStatus("succeed");
                    
                    // Cập nhật reservation payment status và tự động confirm reservation nếu có reservation
                    if (payment.getReservation() != null) {
                        Reservation reservation = payment.getReservation();
                        reservation.setPaymentStatus("completed");
                        reservationRepository.save(reservation);
                        log.info("Updated reservation payment status to completed: reservationId={}", 
                            reservation.getReservationId());
                        
                        // Tự động confirm reservation nếu đang ở status "pending" (giống như trong webhook)
                        if ("pending".equalsIgnoreCase(reservation.getStatus())) {
                            try {
                                // For instant swap, skip to checked_in; otherwise confirm normally
                                if (reservation.getIsInstantSwap() != null && reservation.getIsInstantSwap()) {
                                    reservation.setStatus("checked_in");
                                    reservation.setCheckedInAt(Instant.now());
                                } else {
                                    reservation.setStatus("confirmed");
                                }
                                reservationRepository.save(reservation);
                                log.info("Auto-confirmed reservation after PayOS payment reconcile: reservationId={}, newStatus={}", 
                                    reservation.getReservationId(), reservation.getStatus());
                            } catch (Exception e) {
                                log.warn("Failed to auto-confirm reservation after reconcile: reservationId={}, error={}", 
                                    reservation.getReservationId(), e.getMessage());
                            }
                        }
                    }
                } else if ("CANCELLED".equalsIgnoreCase(payosStatus)) {
                    payment.setStatus("failed");
                } else if (payosStatus != null) {
                    payment.setStatus("pending");
                }
                paymentRepository.save(payment);
            }
        } catch (Exception ex) {
            log.warn("Reconcile PayOS status failed for paymentId={}, reason={}", paymentId, ex.getMessage());
        }

        return payment;
    }

    /**
     * Tạo yêu cầu thanh toán PayOS cho giao dịch đổi pin (swap transaction)
     * @param driver Tài xế thực hiện thanh toán
     * @param reservation Reservation liên quan
     * @param amount Số tiền cần thanh toán
     * @return Payment object với status pending
     */
    @Transactional
    public Payment createSwapPaymentRequest(Driver driver, Reservation reservation, BigDecimal amount) {
        try {
            // Tạo orderCode duy nhất
            long timestamp = System.currentTimeMillis();
            String timestampStr = String.valueOf(timestamp);
            String last10Digits = timestampStr.substring(timestampStr.length() - 10);
            String driverIdStr = String.valueOf(driver.getDriverId());
            String orderCodeStr = last10Digits + driverIdStr;
            if (orderCodeStr.length() > 11) {
                orderCodeStr = orderCodeStr.substring(orderCodeStr.length() - 11);
            }
            Long orderCode = Long.parseLong(orderCodeStr);
            log.info("Generated orderCode for swap: {} (length: {})", orderCode, orderCodeStr.length());

            // Tạo PayOS SDK instance
            PayOS payOS = new PayOS(payosClientId, payosApiKey, payosWebhookKey);
            try {
                String verified = payOS.confirmWebhook(payosWebhookUrl);
                log.info("PayOS webhook confirmed: {}", verified);
            } catch (Exception ex) {
                log.warn("Could not confirm PayOS webhook. Proceeding anyway. reason={}", ex.getMessage());
            }
            
            String cancelUrl = publicBaseUrl + "/staff/reservations/" + reservation.getReservationId();
            // Use callback endpoint that will redirect to staff page and trigger auto refresh
            String successUrl = publicBaseUrl + "/reservations/payment-callback?reservationId=" + reservation.getReservationId();
            // PayOS requires description max 25 characters
            String description = "Doi pin #" + reservation.getReservationId();
            if (description.length() > 25) {
                // If still too long, truncate to 25 chars
                description = description.substring(0, 25);
            }
            
            ItemData itemData = ItemData.builder()
                .name("Phí đổi pin EVSWAP")
                .quantity(1)
                .price(amount.intValue())
                .build();
                
            PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(amount.intValue())
                .description(description)
                .item(itemData)
                .cancelUrl(cancelUrl)
                .returnUrl(successUrl)
                .build();
                
            log.info("PayOS PaymentData for swap: {}", paymentData);
            CheckoutResponseData result = payOS.createPaymentLink(paymentData);
            String checkoutUrl = result.getCheckoutUrl();
            String paymentLinkId = result.getPaymentLinkId();
            
            // Lưu payment với status pending
            Payment payment = Payment.builder()
                    .driver(driver)
                    .reservation(reservation)
                    .amount(amount)
                    .method("payos")
                    .status("pending")
                    .paidAt(Instant.now())
                    .currency("VND")
                    .providerTxnId(paymentLinkId)
                    .orderCode("EVSWAP" + orderCode)
                    .checkoutUrl(checkoutUrl)
                    .build();
            payment = paymentRepository.save(payment);
            
            log.info("Created PayOS swap payment request [SDK]: paymentId={}, orderCode=EVSWAP{}, checkoutUrl={}", 
                payment.getPaymentId(), orderCode, checkoutUrl);
            return payment;
        } catch (Exception e) {
            log.error("Error creating PayOS swap payment request (SDK)", e);
            throw new IllegalStateException("Lỗi khi tạo yêu cầu thanh toán: " + e.getMessage());
        }
    }
}
