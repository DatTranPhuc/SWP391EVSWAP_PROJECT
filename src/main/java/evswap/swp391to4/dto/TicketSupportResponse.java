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
public class TicketSupportResponse {
    
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
    private String commentHistory;
    private String attachments;
}
