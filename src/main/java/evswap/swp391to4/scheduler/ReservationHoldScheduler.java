package evswap.swp391to4.scheduler;

import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationHoldScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationHoldScheduler.class);

    private final ReservationRepository reservationRepository;
    private final BatteryService batteryService;

    @Scheduled(fixedDelayString = "${app.reservation.hold-cleanup-interval-ms:60000}")
    @Transactional
    public void releaseExpiredHolds() {
        Instant now = Instant.now();
        Instant threshold = now.minus(30, ChronoUnit.MINUTES);

        List<Reservation> expiredReservations = reservationRepository
                .findByStatusInAndReservedStartBefore(List.of("pending", "confirmed"), threshold);

        if (expiredReservations.isEmpty()) {
            return;
        }

        for (Reservation reservation : expiredReservations) {
            batteryService.releaseReservationHold(reservation.getReservationId());

            if (!"no_show".equalsIgnoreCase(reservation.getStatus())) {
                reservation.setStatus("no_show");
            }
        }

        reservationRepository.saveAll(expiredReservations);
        log.debug("Released {} expired reservation holds", expiredReservations.size());
    }
}
