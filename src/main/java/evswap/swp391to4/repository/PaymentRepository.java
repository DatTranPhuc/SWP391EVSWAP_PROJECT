package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface để thực hiện các thao tác CRUD trên Payment entity.
 * JpaRepository<Payment, Integer> có nghĩa là nó làm việc với entity Payment
 * và kiểu dữ liệu của khóa chính (paymentId) là Integer.
 */
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /**
     * Tìm tất cả các giao dịch nạp tiền của một tài xế,
     * sắp xếp theo thời gian thanh toán (paidAt) mới nhất lên đầu.
     * Spring Data JPA sẽ tự động tạo câu lệnh query dựa trên tên của phương thức này.
     * @param driverId ID của tài xế cần tìm.
     * @return Một danh sách các giao dịch nạp tiền.
     */
    List<Payment> findByDriver_DriverIdOrderByPaidAtDesc(Integer driverId);
}