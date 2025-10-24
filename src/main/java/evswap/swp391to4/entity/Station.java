package evswap.swp391to4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "station")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "station_id")
    private Integer stationId;

    @Column( columnDefinition = "nvarchar(100)",nullable = false)
    private String name;

    @Column( columnDefinition = "nvarchar(200)")
    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String status; // "active", "closed"
}
