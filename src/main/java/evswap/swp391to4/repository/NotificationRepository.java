package evswap.swp391to4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import evswap.swp391to4.entity.Notification;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    List<Notification> findByDriver_DriverIdOrderBySentAtDesc(Integer driverId);
    
    List<Notification> findByDriver_DriverIdAndIsReadOrderBySentAtDesc(Integer driverId, Boolean isRead);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.driver.driverId = :driverId AND (n.isRead IS NULL OR n.isRead = false)")
    long countUnreadByDriverId(@Param("driverId") Integer driverId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.driver.driverId = :driverId AND (n.isRead IS NULL OR n.isRead = false)")
    int markAllAsReadByDriverId(@Param("driverId") Integer driverId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notiId = :notiId AND n.driver.driverId = :driverId")
    int markAsReadByIdAndDriverId(@Param("notiId") Integer notiId, @Param("driverId") Integer driverId);
    
    void deleteByDriver_DriverIdAndIsReadTrue(Integer driverId);
}
