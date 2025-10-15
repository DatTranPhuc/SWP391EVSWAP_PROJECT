package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    // Lấy tất cả thanh toán của một driver
    List<Payment> findByDriverDriverId(Integer driverId);
    
    // Lấy thanh toán theo trạng thái
    List<Payment> findByDriverDriverIdAndStatus(Integer driverId, String status);
    
    // Tính tổng số tiền đã thanh toán thành công của một driver
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.driver.driverId = :driverId AND p.status = 'succeed'")
    BigDecimal getTotalPaidAmountByDriver(@Param("driverId") Integer driverId);
    
    // Tính tổng số tiền đã hoàn lại của một driver
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.driver.driverId = :driverId AND p.status = 'refunded'")
    BigDecimal getTotalRefundedAmountByDriver(@Param("driverId") Integer driverId);
    
    // Đếm số lần thanh toán thành công
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.driver.driverId = :driverId AND p.status = 'succeed'")
    Long getSuccessfulPaymentCountByDriver(@Param("driverId") Integer driverId);
    
    // Lấy thanh toán gần đây nhất
    @Query("SELECT p FROM Payment p WHERE p.driver.driverId = :driverId ORDER BY p.paidAt DESC")
    List<Payment> getRecentPaymentsByDriver(@Param("driverId") Integer driverId);
}
