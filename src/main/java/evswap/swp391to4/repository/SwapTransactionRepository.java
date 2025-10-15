package evswap.swp391to4.repository;

import evswap.swp391to4.entity.SwapTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SwapTransactionRepository extends JpaRepository<SwapTransaction, Integer> {
    Optional<SwapTransaction> findByReservationReservationId(Integer reservationId);
    List<SwapTransaction> findByStationStationIdOrderBySwappedAtDesc(Integer stationId);
}
