package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByDriverDriverIdOrderBySentAtDesc(Integer driverId);
    long countByDriverDriverIdAndIsReadFalse(Integer driverId);
}
