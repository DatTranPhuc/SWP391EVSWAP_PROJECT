package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class FeedbackResponse {
    private Integer feedbackId;
    private Integer driverId;
    private String driverName;
    private Integer stationId;
    private String stationName;
    private Integer rating;
    private String comment;
    private Instant createdAt;
}
