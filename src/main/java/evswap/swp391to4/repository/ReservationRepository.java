package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByDriverDriverIdOrderByReservedStartAsc(Integer driverId);
}
