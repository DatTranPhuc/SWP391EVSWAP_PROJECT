package evswap.swp391to4.scheduler;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    private final PaymentService paymentService;

    /**
     * Tự động hủy reservation quá hạn
     * Chạy mỗi 5 phút
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void autoCancelOverdueReservations() {
        try {
            // Tìm reservations quá hạn (pending hoặc confirmed mà reservedStart + 30 phút < now)
            Instant cutoffTime = Instant.now().minusSeconds(30 * 60); // 30 minutes ago
            
            List<Reservation> overdueReservations = reservationRepository.findAll().stream()
                .filter(reservation -> 
                    ("pending".equals(reservation.getStatus()) || "confirmed".equals(reservation.getStatus())) &&
                    reservation.getReservedStart() != null &&
                    reservation.getReservedStart().isBefore(cutoffTime)
                )
                .toList();

            for (Reservation reservation : overdueReservations) {
                try {
                    // Đánh dấu là no_show
                    reservation.setStatus("no_show");
                    reservationRepository.save(reservation);

                    // Hoàn tiền 0% (hoặc có thể hoàn 60% tùy business logic)
                    // paymentService.simulateRefund(reservation.getDriver(), reservation, BigDecimal.ZERO);
                    
                    log.info("Auto-cancelled overdue reservation: {}", reservation.getReservationId());
                } catch (Exception e) {
                    log.error("Error auto-cancelling reservation {}: {}", reservation.getReservationId(), e.getMessage());
                }
            }

            if (!overdueReservations.isEmpty()) {
                log.info("Auto-cancelled {} overdue reservations", overdueReservations.size());
            }
        } catch (Exception e) {
            log.error("Error in auto-cancel scheduler: {}", e.getMessage());
        }
    }
}
