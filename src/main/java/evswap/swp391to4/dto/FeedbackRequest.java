package evswap.swp391to4.dto;

import lombok.Data;

@Data
public class FeedbackRequest {
    private Integer driverId;
    private Integer stationId;
    private Integer rating;
    private String comment;
}
