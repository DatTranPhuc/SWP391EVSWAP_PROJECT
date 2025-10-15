package evswap.swp391to4.dto;

import lombok.Data;

@Data
public class NotificationRequest {
    private Integer driverId;
    private String type;
    private String title;
    private Integer reservationId;
    private Integer paymentId;
}
