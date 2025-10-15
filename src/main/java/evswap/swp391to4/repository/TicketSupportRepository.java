package evswap.swp391to4.repository;

import evswap.swp391to4.entity.TicketSupport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketSupportRepository extends JpaRepository<TicketSupport, Integer> {
    List<TicketSupport> findByDriverDriverIdOrderByCreatedAtDesc(Integer driverId);
    List<TicketSupport> findByStaffStaffIdOrderByCreatedAtDesc(Integer staffId);
    List<TicketSupport> findByStatusOrderByCreatedAtDesc(String status);
}
