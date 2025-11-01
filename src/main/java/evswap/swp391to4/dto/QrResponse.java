package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrResponse {
    private String qrToken;
    private String qrStatus;
    private Long expiresAt;
}


