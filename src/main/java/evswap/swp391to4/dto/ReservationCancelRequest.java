package evswap.swp391to4.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCancelRequest {
    
    @NotNull(message = "Reservation ID không được để trống")
    private Integer reservationId;
    
    private String reason; // optional
}
