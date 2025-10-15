package evswap.swp391to4.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ReservationStatusUpdateRequest {
    private String status;
    private Instant checkedInAt;
}
