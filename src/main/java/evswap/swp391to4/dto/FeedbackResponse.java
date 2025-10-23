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