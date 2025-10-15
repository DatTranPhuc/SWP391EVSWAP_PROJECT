package evswap.swp391to4.dto;

import lombok.Data;

@Data
public class TicketUpdateRequest {
    private Integer staffId;
    private String status;
    private String note;
}
