package evswap.swp391to4.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Integer notiId;
    private String type;
    private String title;
    private Boolean isRead;
    private Instant sentAt;
    private Integer reservationId;
    private String reservationStatus;
    private String stationName;
    private Integer paymentId;
}
