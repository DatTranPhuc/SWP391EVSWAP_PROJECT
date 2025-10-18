package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Integer> {

    Optional<Station> findByNameIgnoreCase(String name);
    List<Station> findByNameContainingIgnoreCase(String keyword);
    List<Station> findByStatus(String status);

    @Query(value =
            // Bắt đầu một Common Table Expression (CTE) tên là StationWithDistance
            "WITH StationWithDistance AS ( " +
                    "    SELECT " +
                    "        *, " + // Chọn tất cả các cột gốc từ bảng station
                    "        ( 6371 * acos( cos( radians(:lat) ) * cos( radians(latitude) ) * cos( radians(longitude) - radians(:lng) ) + sin( radians(:lat) ) * sin( radians(latitude) ) ) ) AS distance " + // Và tính khoảng cách
                    "    FROM station " +
                    "    WHERE latitude IS NOT NULL AND longitude IS NOT NULL " +
                    ") " +
                    // Bây giờ, truy vấn từ CTE nơi mà cột 'distance' đã tồn tại
                    "SELECT " +
                    "    s.station_id as stationId, s.name, s.address, s.status, s.latitude, s.longitude, s.distance " +
                    "FROM StationWithDistance s " +
                    "WHERE s.distance < :radiusKm " +
                    "ORDER BY s.distance",
            nativeQuery = true)
    List<StationDistance> findNearbyStations(@Param("lat") double lat, @Param("lng") double lng, @Param("radiusKm") double radiusKm);
}