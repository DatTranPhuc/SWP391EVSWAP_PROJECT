package evswap.swp391to4.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Table(name = "battery")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Battery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "battery_id")
    private Integer batteryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false)
    private String model;

    @Column(name = "vehicle_type")
    private String vehicleType; // motorcycle/car

    @Column(columnDefinition = "NVARCHAR(50)")
    private String state;     // full/charging/maintenance/retired
    @Column(name = "soh_percent")
    private Integer sohPercent;
    @Column(name = "soc_percent")
    private Integer socPercent;
}
