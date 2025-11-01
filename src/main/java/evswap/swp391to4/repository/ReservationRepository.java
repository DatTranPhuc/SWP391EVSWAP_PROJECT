package evswap.swp391to4.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import evswap.swp391to4.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByDriverDriverIdOrderByReservedStartAsc(Integer driverId);
    List<Reservation> findByStationStationIdOrderByReservedStartAsc(Integer stationId);
}
