package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {
    private Integer notificationId;
    private Integer driverId;
    private String type;
    private String title;
    private Boolean read;
    private Instant sentAt;
    private Integer reservationId;
    private Integer paymentId;
}
