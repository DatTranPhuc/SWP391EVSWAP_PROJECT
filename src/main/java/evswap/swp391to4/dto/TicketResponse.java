package evswap.swp391to4.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TicketResponse {
    private Integer ticketId;
    private Integer driverId;
    private String driverName;
    private Integer staffId;
    private String staffName;
    private String category;
    private String comment;
    private String status;
    private Instant createdAt;
    private Instant resolvedAt;
    private String note;
}
