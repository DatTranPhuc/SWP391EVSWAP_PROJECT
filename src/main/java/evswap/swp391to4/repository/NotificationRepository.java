package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findTop10ByDriverDriverIdOrderBySentAtDesc(Integer driverId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.driver.driverId = :driverId AND (n.isRead = false OR n.isRead IS NULL)")
    long countUnreadByDriverId(@Param("driverId") Integer driverId);

    Optional<Notification> findByNotiIdAndDriverDriverId(Integer notificationId, Integer driverId);

    @Query("SELECT n FROM Notification n WHERE n.driver.driverId = :driverId AND (n.isRead = false OR n.isRead IS NULL)")
    List<Notification> findUnreadByDriverId(@Param("driverId") Integer driverId);
}
