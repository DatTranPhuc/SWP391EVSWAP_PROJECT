package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Battery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatteryRepository extends JpaRepository<Battery, Integer> {
    List<Battery> findByStationStationId(Integer stationId);
    List<Battery> findByState(String state);
}
