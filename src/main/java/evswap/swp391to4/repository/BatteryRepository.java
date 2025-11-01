package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatteryRepository extends JpaRepository<Battery, Integer> {

    /**
     * Tìm tất cả pin thuộc về một Station cụ thể.
     * Spring Data JPA sẽ tự động hiểu "findBy" + "Station".
     * @param station Đối tượng Station mà bạn muốn tìm pin.
     * @return Danh sách pin thuộc trạm đó.
     */
    List<Battery> findByStation(Station station);

    /**
     * Tìm pin theo trạm VÀ trạng thái (có thể lọc).
     * Ví dụ: tìm tất cả pin ở trạm X có trạng thái là "charging".
     * 'ContainingIgnoreCase' cho phép tìm kiếm linh hoạt (ví dụ: "full" sẽ khớp "full").
     */
    List<Battery> findByStationAndStateContainingIgnoreCase(Station station, String state);

    // ===== HÀM MỚI (Cho Dashboard) =====
    /**
     * Đếm số lượng pin tại 1 trạm theo 1 trạng thái cụ thể.
     * @return Số lượng (long).
     */
    long countByStationAndState(Station station, String state);

    long countByStationAndStateAndReservedForReservationIdIsNull(Station station, String state);

    long countByStationAndReservedForReservationIdIsNotNull(Station station);
    /**
     * Tìm pin theo ID (dù ID là duy nhất, trả về List cho đồng bộ)
     */
    List<Battery> findByStationAndBatteryId(Station station, Integer batteryId);

    /**
     * Tìm pin theo Model
     */
    List<Battery> findByStationAndModelContainingIgnoreCase(Station station, String model);

    List<Battery> findByReservedForReservationId(Integer reservationId);

    /**
     * Tìm pin đủ điều kiện cho reservation
     */
    List<Battery> findByStationStationIdAndStateAndSocPercentAndSohPercentGreaterThanEqualAndReservedForReservationIdIsNull(
        Integer stationId, String state, Integer socPercent, Integer sohPercent);
}
