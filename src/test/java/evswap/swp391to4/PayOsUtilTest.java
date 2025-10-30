package evswap.swp391to4;

import evswap.swp391to4.util.PayOsUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PayOsUtilTest {
    /**
     * Test signature using the exact fields and order as PayOS official sample:
     * amount|orderCode|description|accountNumber|reference|transactionDateTime|currency|paymentLinkId|code|desc|counterAccountBankId|counterAccountBankName|counterAccountName|counterAccountNumber|virtualAccountName|virtualAccountNumber
     */
    @Test
    public void testCreateHmacSignature_PayOsExample() {
        String checksumKey = "1a54716c8f0efb2744fb28b6e38b25da7f67a925d98bc1c18bd8faaecadd7675";
        // Change data string to match exactly PayOS PHP/JS sample
        String data = "3000|123|VQRIO123|12345678|TF230204212323|2023-02-04 18:25:00|VND|124c33293c43417ab7879e14c8d9eb18|00|Thành công||||||";
        String expectedSignature = "412e915d2871504ed31be63c8f62a149a4410d34c4c42affc9006ef9917eaa03";
        String realSignature = PayOsUtil.createHmacSignature(data, checksumKey);
        System.out.println("DEBUG: realSignature=" + realSignature);
        Assertions.assertEquals(expectedSignature, realSignature,
                "PayOS signature does not match example from docs!");
    }

    @Test
    public void testCreateHmacSignature_BlankValue() {
        String checksumKey = "1a54716c8f0efb2744fb28b6e38b25da7f67a925d98bc1c18bd8faaecadd7675";
        String data = "3000|||VQRIO123|123|||"; // fields blank/null become empty strings
        String sig = PayOsUtil.createHmacSignature(data, checksumKey);
        Assertions.assertNotNull(sig);
        Assertions.assertEquals(64, sig.length(), "Signature must have 64 hex chars");
    }

    @Test
    public void testCreateHmacSignature_PayOsExampleEncoded() {
        String checksumKey = "testkey0123456789testkey0123456789testkey0123456789testkey0123456789";
        String dataToSign = PayOsUtil.buildPaymentRequestSignString(
            3000L,
            "http://cancel-url",
            "client-abc",
            "Thanh toán test Encoding",
            123L,
            "http://success-url",
            "http://webhook-url"
        );
        String signature = PayOsUtil.createHmacSignature(dataToSign, checksumKey);
        System.out.println("[Encoded Example SIGNATURE] " + signature);
        Assertions.assertNotNull(signature);
        Assertions.assertEquals(64, signature.length(), "Signature must be 64 hex chars!");
    }
}
