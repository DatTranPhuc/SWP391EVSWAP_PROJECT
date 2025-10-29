package evswap.swp391to4.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDetailResponse {
    
    private Integer reservationId;
    private Integer driverId;
    private String driverName;
    private String driverPhone;
    
    private Integer stationId;
    private String stationName;
    private String stationAddress;
    
    private Integer vehicleId;
    private String vehicleModel;
    private String vehiclePlateNumber;
    
    private Integer assignedBatteryId;
    private String batteryModel;
    private Integer batterySoh;
    private Integer batterySoc;
    private String batteryState;
    
    private BigDecimal priceAmount;
    private Instant reservedStart;
    private String status;
    private Instant createdAt;
    private Instant checkedInAt;
    
    // QR Code info
    private String qrToken;
    private String qrStatus;
    private Instant qrExpiresAt;
    
    // Payment info
    private Integer paymentId;
    private String paymentStatus;
    private Instant paymentDate;
}
