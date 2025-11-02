package evswap.swp391to4.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class QRCodeService {

    private static final int QR_CODE_SIZE = 400;
    private static final String CHARSET = "UTF-8";

    /**
     * Tạo QR code image từ token
     * @param token Token để encode vào QR code
     * @return Byte array của QR code image (PNG format)
     */
    public byte[] generateQrCodeImage(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token không được để trống");
        }

        try {
            // Cấu hình QR code
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
            hints.put(EncodeHintType.MARGIN, 1);

            // Tạo QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(token, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

            // Chuyển BitMatrix thành BufferedImage
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            graphics.dispose();

            // Chuyển BufferedImage thành byte array (PNG)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();

        } catch (WriterException e) {
            log.error("Lỗi khi tạo QR code: {}", e.getMessage(), e);
            throw new IllegalStateException("Không thể tạo QR code: " + e.getMessage());
        } catch (IOException e) {
            log.error("Lỗi khi ghi QR code image: {}", e.getMessage(), e);
            throw new IllegalStateException("Không thể tạo QR code image: " + e.getMessage());
        }
    }

    /**
     * Đọc QR code từ ảnh và trả về token
     * @param imageBytes Byte array của ảnh chứa QR code
     * @return Token string từ QR code
     */
    public String decodeQrCodeFromImage(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Image data không được để trống");
        }

        try {
            // Đọc ảnh từ byte array
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new IllegalStateException("Không thể đọc ảnh từ dữ liệu được cung cấp");
            }

            // Sử dụng ZXing để decode QR code
            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap, hints);
            return result.getText();

        } catch (NotFoundException e) {
            log.warn("Không tìm thấy QR code trong ảnh: {}", e.getMessage());
            throw new IllegalStateException("Không tìm thấy QR code trong ảnh. Vui lòng đảm bảo ảnh chứa QR code hợp lệ.");
        } catch (Exception e) {
            log.error("Lỗi khi đọc QR code từ ảnh: {}", e.getMessage(), e);
            if (e instanceof FormatException || e instanceof ChecksumException) {
                throw new IllegalStateException("QR code không hợp lệ hoặc bị hỏng.");
            }
            throw new IllegalStateException("Không thể đọc QR code từ ảnh: " + e.getMessage());
        }
    }
}

