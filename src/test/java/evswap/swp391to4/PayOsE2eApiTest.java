package evswap.swp391to4;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.repository.PaymentRepository;
import evswap.swp391to4.service.PaymentService;
import evswap.swp391to4.util.PayOsUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.math.BigDecimal;
import java.util.Optional;

@SpringBootTest
public class PayOsE2eApiTest {
    @Autowired
    private PaymentService paymentService;

    @MockBean
    private PaymentRepository paymentRepository;

    private Driver testDriver;

    @BeforeEach
    public void setup() {
        testDriver = new Driver();
        testDriver.setDriverId(123);
        testDriver.setFullName("PayOs JUnit E2E Tester");
    }

    @Test
    public void testCreatePaymentAndWebhookFlow() {
        // Step 1: Giả lập tạo payment request (thanh toán qua PayOS)
        BigDecimal amount = new BigDecimal("50000");
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setMethod("payos");
        payment.setStatus("pending");
        payment.setOrderCode("999888777666");
        payment.setDriver(testDriver);
        Mockito.when(paymentRepository.save(Mockito.any())).thenReturn(payment);

        // (Ở đây giả lập PaymentService trả về Payment entity, không gọi thật ra PayOS)
        // Check signature build đúng chuẩn encode
        String cancelUrl = "http://cancel.test";
        String clientId = "c-test123";
        String desc = "Đơn vị test PayOS";
        long orderCode = 999888777666L;
        String successUrl = "http://success.test";
        String webhookUrl = "http://webhook.test";
        String secretKey = "testkeyabcdefg0123456testkeyabcdefg0123456testkeyabcdefg0123456testkeyabcdefg0123456";

        String dataToSign = PayOsUtil.buildPaymentRequestSignString(
            amount.longValue(), cancelUrl, clientId, desc, orderCode, successUrl, webhookUrl);
        String sign = PayOsUtil.createHmacSignature(dataToSign, secretKey);
        Assertions.assertEquals(64, sign.length());

        // Step 2: Giả lập nhận webhook PayOS về, verify signature đúng
        // (Giả lập signature hợp lệ)
        String webhookCode = "00";
        String webhookDesc = "success";
        String status = "PAID";
        String webhookDataStr = webhookCode + "|" + webhookDesc + "|" + orderCode + "|" + amount.longValue() + "|" + status;
        String webhookSig = PayOsUtil.createHmacSignature(webhookDataStr, secretKey);

        // Giả lập PaymentRepository tìm được payment qua orderCode
        Mockito.when(paymentRepository.findByOrderCode(Mockito.anyString()))
                .thenReturn(Optional.of(payment));
        Mockito.when(paymentRepository.save(Mockito.any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Xử lý webhook, passing all params
        // Object webhookReq = (Tùy refactor: nên pass một Pojo có fields code, desc, data, signature...)
        // Nếu có DTO WebhookRequest riêng, tạo instance và gán field tương ứng, gọi paymentService.handlePayOsWebhook ...
        // Ở đây test đơn giản chỉ verify hàm verifySignature
        boolean valid = PayOsUtil.verifySignature(webhookSig, webhookDataStr, secretKey);
        Assertions.assertTrue(valid, "Webhook signature must be valid!");
    }
}
