package evswap.swp391to4.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import evswap.swp391to4.entity.SwapTransaction;

@Repository
public interface SwapTransactionRepository extends JpaRepository<SwapTransaction, Integer> {

    List<SwapTransaction> findByReservation_Driver_DriverIdAndSwappedAtAfter(Integer driverId, Instant from);

    boolean existsByReservation_Driver_DriverIdAndSwappedAtAfter(Integer driverId, Instant from);

    List<SwapTransaction> findByStation_StationIdAndSwappedAtBetween(Integer stationId, Instant from, Instant to);

    Optional<SwapTransaction> findByReservation_ReservationId(Integer reservationId);
}


