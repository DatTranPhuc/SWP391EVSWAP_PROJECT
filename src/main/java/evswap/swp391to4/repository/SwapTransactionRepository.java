package evswap.swp391to4.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.entity.SwapTransaction;

@Repository
public interface SwapTransactionRepository extends JpaRepository<SwapTransaction, Integer> {
    Optional<SwapTransaction> findByReservation(Reservation reservation);

    @Query("select distinct st.station from SwapTransaction st " +
           "where st.reservation.driver.driverId = :driverId " +
           "and st.result = 'success' " +
           "and st.swappedAt between :from and :to")
    List<Station> findEligibleStations(@Param("driverId") Integer driverId,
                                       @Param("from") Instant from,
                                       @Param("to") Instant to);

    boolean existsByReservationDriverDriverIdAndStationStationIdAndResultAndSwappedAtBetween(
            Integer driverId, Integer stationId, String result, Instant from, Instant to);

    @Query("select st.station from SwapTransaction st " +
           "where st.reservation.driver.driverId = :driverId " +
           "and st.result = 'success' " +
           "and st.swappedAt between :from and :to " +
           "order by st.swappedAt desc")
    java.util.List<Station> findRecentStationsOrderedBySwapTime(@Param("driverId") Integer driverId,
                                                                 @Param("from") Instant from,
                                                                 @Param("to") Instant to);
}


