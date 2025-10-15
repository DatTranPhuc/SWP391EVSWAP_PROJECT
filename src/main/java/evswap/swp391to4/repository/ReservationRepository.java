package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByDriverDriverIdOrderByReservedStartDesc(Integer driverId);
    List<Reservation> findByStationStationIdAndReservedStartBetween(Integer stationId, Instant start, Instant end);
    Optional<Reservation> findByQrToken(String qrToken);
}
