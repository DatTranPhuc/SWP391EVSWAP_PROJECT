package evswap.swp391to4.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

@Data
public class FeedbackRequest {
    private Integer driverId;
    private Integer stationId;
    private Integer rating;
    private Integer comment;
}
