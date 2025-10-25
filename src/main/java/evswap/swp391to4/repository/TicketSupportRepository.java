package evswap.swp391to4.repository;

import evswap.swp391to4.entity.TicketSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TicketSupportRepository extends JpaRepository<TicketSupport, Integer> {
    
    /**
     * Lấy tất cả ticket của một driver, sắp xếp theo thời gian tạo mới nhất
     */
    List<TicketSupport> findByDriverDriverIdOrderByCreatedAtDesc(Integer driverId);
    
    /**
     * Lấy tất cả ticket, sắp xếp theo thời gian tạo mới nhất
     */
    List<TicketSupport> findAllByOrderByCreatedAtDesc();
    
    /**
     * Lấy ticket theo trạng thái, sắp xếp theo thời gian tạo mới nhất
     */
    List<TicketSupport> findByStatusOrderByCreatedAtDesc(String status);
    
    /**
     * Lấy ticket được giao cho một staff cụ thể
     */
    List<TicketSupport> findByStaffStaffIdOrderByCreatedAtDesc(Integer staffId);
    
    /**
     * Lấy ticket theo danh mục, sắp xếp theo thời gian tạo mới nhất
     */
    List<TicketSupport> findByCategoryOrderByCreatedAtDesc(String category);
    
    /**
     * Đếm số ticket của driver được tạo sau thời điểm cụ thể
     */
    long countByDriverDriverIdAndCreatedAtAfter(Integer driverId, Instant createdAt);
}