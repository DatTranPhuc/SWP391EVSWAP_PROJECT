package evswap.swp391to4.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class ReservationScheduleForm {
    private Integer stationId;
    private Integer vehicleId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime time;
    
    private String paymentMethod; // "wallet", "cash", "transfer"
}
