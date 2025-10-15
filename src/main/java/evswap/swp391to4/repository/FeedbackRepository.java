package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    List<Feedback> findByStationStationIdOrderByCreatedAtDesc(Integer stationId);
    List<Feedback> findByDriverDriverIdOrderByCreatedAtDesc(Integer driverId);
}
