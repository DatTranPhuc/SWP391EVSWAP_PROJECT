package evswap.swp391to4.service;

import java.math.BigDecimal;
import java.time.Instant;
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
import evswap.swp391to4.entity.Driver; // <-- (IMPORT MỚI)
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.repository.DriverRepository; // <-- (IMPORT MỚI)
import evswap.swp391to4.repository.PaymentRepository;
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
    private final DriverRepository driverRepository; // <-- (THÊM 1: INJECT DRIVER REPO)
    private final RestTemplate restTemplate;

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
     * (Giữ nguyên hàm createPayment)
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

    /**
     * (Giữ nguyên hàm createPayOsTopUpRequest)
     */
    @Transactional
    public Payment createPayOsTopUpRequest(Driver driver, BigDecimal amount) {
        try {
            // (Giữ nguyên code tạo PayOS request của bạn)
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

            PayOS payOS = new PayOS(payosClientId, payosApiKey, payosWebhookKey);
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
     * (ĐÃ SỬA: Thêm logic cộng tiền vào số dư)
     */
    @Transactional
    public boolean handlePayOsWebhook(PayOsWebhookRequest webhookRequest) {
        try {
            Long orderCode = webhookRequest.getData().getOrderCode();
            if (orderCode == null) {
                log.error("Invalid webhook: missing orderCode");
                return false;
            }

            String orderCodeWithPrefix = "EVSWAP" + orderCode;
            Optional<Payment> paymentOpt = paymentRepository.findByOrderCode(orderCodeWithPrefix);

            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for orderCode: {}", orderCode);
                return false;
            }

            Payment payment = paymentOpt.get();
            String oldStatus = payment.getStatus(); // Lấy trạng thái CŨ

            // (Giữ nguyên logic xác thực Signature...)

            // Cập nhật status dựa trên webhook status
            String payosStatus = webhookRequest.getData().getStatus();

            // (SỬA LẠI LOGIC FALLBACK CHO ĐÚNG)
            if (payosStatus == null || payosStatus.isBlank()) {
                try {
                    Payment reconciled = reconcilePayOsPaymentById(payment.getPaymentId());
                    payosStatus = reconciled.getStatus(); // Lấy status mới nhất sau khi reconcile
                } catch (Exception ex) {
                    log.warn("Reconcile fallback failed for orderCode={}, reason={}", orderCode, ex.getMessage());
                }
            }

            if ("PAID".equalsIgnoreCase(payosStatus) || "succeed".equalsIgnoreCase(payosStatus)) {
                payment.setStatus("succeed");
                log.info("Payment successful: paymentId={}, orderCode={}",
                        payment.getPaymentId(), orderCode);

                // ==================================================
                // ===== (THÊM 2: CẬP NHẬT SỐ DƯ CHO DRIVER) =====
                // ==================================================
                // Chỉ cộng tiền NẾU trạng thái cũ chưa phải là 'succeed'
                if (!"succeed".equalsIgnoreCase(oldStatus)) {
                    Driver driver = payment.getDriver();
                    BigDecimal amount = payment.getAmount();

                    driver.setBalance(driver.getBalance().add(amount)); // Cộng tiền
                    driverRepository.save(driver); // Lưu lại Driver

                    log.info("CẬP NHẬT SỐ DƯ (WEBHOOK): Driver ID {} | +{} VNĐ | Số dư mới: {}",
                            driver.getDriverId(), amount, driver.getBalance());
                } else {
                    log.warn("Webhook received for already succeeded paymentId: {}. Ignoring balance update.", payment.getPaymentId());
                }
                // ==================================================

            } else if ("CANCELLED".equalsIgnoreCase(payosStatus)) {
                payment.setStatus("failed");
                log.info("Payment cancelled: paymentId={}, orderCode={}",
                        payment.getPaymentId(), orderCode);
            } else {
                payment.setStatus("pending");
                log.info("Payment still pending: paymentId={}, orderCode={}, status={}",
                        payment.getPaymentId(), orderCode, payosStatus);
            }

            paymentRepository.save(payment); // Lưu payment
            return true;

        } catch (Exception e) {
            log.error("Unexpected error handling PayOS webhook", e);
            return false;
        }
    }

    /**
     * (Giữ nguyên các hàm get...)
     */
    @Transactional(readOnly = true)
    public Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy payment"));
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByIdAndDriver(Integer paymentId, Driver driver) {
        return paymentRepository.findByPaymentIdAndDriver(paymentId, driver)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy payment hoặc không có quyền truy cập"));
    }

    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentByProviderTxnId(String providerTxnId) {
        return paymentRepository.findByProviderTxnId(providerTxnId);
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
     * (ĐÃ SỬA: Thêm logic cộng tiền vào số dư)
     */
    @Transactional
    public Payment reconcilePayOsPaymentById(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy payment"));

        if (!"payos".equalsIgnoreCase(payment.getMethod())) {
            return payment; // Không phải giao dịch PayOS
        }

        // (Giữ nguyên logic lấy numericOrderCode)
        String orderCodeStr = payment.getOrderCode();
        if (orderCodeStr == null || !orderCodeStr.startsWith("EVSWAP")) {
            return payment;
        }
        String numericOrderCode = orderCodeStr.substring("EVSWAP".length());

        // (GiGữ nguyên logic gọi API PayOS)
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

                String oldStatus = payment.getStatus(); // Lấy trạng thái CŨ

                if ("PAID".equalsIgnoreCase(payosStatus)) {
                    payment.setStatus("succeed");

                    // ==================================================
                    // ===== (THÊM 3: CẬP NHẬT SỐ DƯ CHO DRIVER) =====
                    // ==================================================
                    // Chỉ cộng tiền NẾU trạng thái cũ chưa phải là 'succeed'
                    if (!"succeed".equalsIgnoreCase(oldStatus)) {
                        Driver driver = payment.getDriver();
                        BigDecimal amount = payment.getAmount();

                        driver.setBalance(driver.getBalance().add(amount)); // Cộng tiền
                        driverRepository.save(driver); // Lưu lại Driver

                        log.info("CẬP NHẬT SỐ DƯ (RECONCILE): Driver ID {} | +{} VNĐ | Số dư mới: {}",
                                driver.getDriverId(), amount, driver.getBalance());
                    }
                    // ==================================================

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
}