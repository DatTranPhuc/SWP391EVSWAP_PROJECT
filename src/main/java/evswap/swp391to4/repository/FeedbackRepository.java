package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Feedback;
import evswap.swp391to4.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    
    /**
     * Tìm tất cả feedback của một driver
     */
    List<Feedback> findByDriver(Driver driver);
    
    /**
     * Tìm tất cả feedback của một station
     */
    List<Feedback> findByStation(Station station);
    
    /**
     * Lấy tất cả feedback sắp xếp theo thời gian tạo (mới nhất trước)
     */
    List<Feedback> findAllByOrderByCreatedAtDesc();
    
    /**
     * Tìm feedback theo driver ID
     */
    List<Feedback> findByDriverDriverId(Integer driverId);
    
    /**
     * Tìm feedback theo station ID
     */
    List<Feedback> findByStationStationId(Integer stationId);
    
    /**
     * Tìm feedback theo station ID và sắp xếp theo thời gian tạo (mới nhất trước)
     */
    List<Feedback> findByStationStationIdOrderByCreatedAtDesc(Long stationId);
}