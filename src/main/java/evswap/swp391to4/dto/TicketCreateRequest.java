package evswap.swp391to4.dto;

import lombok.Data;

@Data
public class TicketCreateRequest {
    private Integer driverId;
    private Integer staffId;
    private String category;
    private String comment;
}
