package evswap.swp391to4.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryReassignRequest {
    
    @NotNull(message = "Reservation ID không được để trống")
    private Integer reservationId;
    
    @NotNull(message = "Battery ID không được để trống")
    private Integer newBatteryId;
}
