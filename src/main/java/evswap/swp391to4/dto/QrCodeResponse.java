package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrCodeResponse {
    private String qrCodeUrl;     // URL đến file ảnh QR code (để hiển thị trong <img>)
    private String deepLink;      // Link để mở trực tiếp ứng dụng (ví dụ: momo://...)
}