package evswap.swp391to4.service;

import evswap.swp391to4.dto.NotificationResponse;
import evswap.swp391to4.dto.PaymentResponse;
import evswap.swp391to4.dto.ReservationResponse;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.dto.SwapTransactionResponse;
import evswap.swp391to4.dto.TicketResponse;
import evswap.swp391to4.dto.dashboard.DashboardSnapshot;
import evswap.swp391to4.entity.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class DashboardDataService {

    private static final Set<String> UPCOMING_STATUSES = Set.of("pending", "confirmed");

    private final VehicleService vehicleService;
    private final ReservationService reservationService;
    private final SwapTransactionService swapTransactionService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final TicketSupportService ticketSupportService;
    private final StationService stationService;

    public DashboardSnapshot buildSnapshot(Integer driverId) {
        List<Vehicle> vehicles = vehicleService.getVehiclesForDriver(driverId);
        List<ReservationResponse> reservations = reservationService.getReservationsForDriver(driverId);
        List<SwapTransactionResponse> swaps = swapTransactionService.listByDriver(driverId);
        List<PaymentResponse> payments = paymentService.getPaymentsForDriver(driverId);
        List<NotificationResponse> notifications = notificationService.getNotificationsForDriver(driverId);
        List<TicketResponse> tickets = ticketSupportService.getTicketsForDriver(driverId);
        List<StationResponse> stations = stationService.getAllStations().stream()
                .filter(station -> station.getStatus() == null || !"inactive".equalsIgnoreCase(station.getStatus()))
                .sorted(Comparator.comparing(
                        station -> Optional.ofNullable(station.getName()).orElse(""),
                        String.CASE_INSENSITIVE_ORDER))
                .limit(6)
                .toList();

        DashboardSnapshot.Overview overview = buildOverview(driverId, vehicles.size(), reservations, swaps, payments);

        AtomicInteger index = new AtomicInteger(1);
        List<DashboardSnapshot.VehicleCard> vehicleCards = vehicles.stream()
                .map(vehicle -> new DashboardSnapshot.VehicleCard(
                        vehicle.getVehicleId(),
                        Optional.ofNullable(vehicle.getModel())
                                .filter(model -> !model.isBlank())
                                .orElse("Phương tiện #" + index.getAndIncrement()),
                        vehicle.getVin(),
                        vehicle.getPlateNumber(),
                        vehicle.getModel(),
                        vehicle.getCreatedAt()
                ))
                .toList();

        return new DashboardSnapshot(
                overview,
                vehicleCards,
                reservations,
                swaps,
                payments,
                notifications,
                tickets,
                stations
        );
    }

    private DashboardSnapshot.Overview buildOverview(Integer driverId,
                                                      int totalVehicles,
                                                      List<ReservationResponse> reservations,
                                                      List<SwapTransactionResponse> swaps,
                                                      List<PaymentResponse> payments) {
        BigDecimal totalPaid = payments.stream()
                .filter(payment -> payment.getAmount() != null)
                .filter(payment -> payment.getStatus() != null && "succeed".equalsIgnoreCase(payment.getStatus()))
                .map(PaymentResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long unreadNotifications = notificationService.countUnread(driverId);

        DashboardSnapshot.Overview.NextReservation nextReservation = reservations.stream()
                .filter(reservation -> reservation.getReservedStart() != null && reservation.getReservedStart().isAfter(Instant.now()))
                .min(Comparator.comparing(ReservationResponse::getReservedStart))
                .map(reservation -> new DashboardSnapshot.Overview.NextReservation(
                        reservation.getReservationId(),
                        reservation.getStationName(),
                        reservation.getReservedStart(),
                        reservation.getStatus()
                ))
                .orElse(null);

        DashboardSnapshot.Overview.LastPayment lastPayment = payments.stream()
                .filter(payment -> payment.getPaidAt() != null)
                .max(Comparator.comparing(PaymentResponse::getPaidAt))
                .map(payment -> new DashboardSnapshot.Overview.LastPayment(
                        payment.getPaymentId(),
                        payment.getAmount(),
                        payment.getMethod(),
                        payment.getStatus(),
                        payment.getPaidAt()
                ))
                .orElse(null);

        int upcomingReservations = (int) reservations.stream()
                .filter(reservation -> reservation.getStatus() != null)
                .map(reservation -> reservation.getStatus().toLowerCase())
                .filter(UPCOMING_STATUSES::contains)
                .count();

        int completedSwaps = (int) swaps.stream()
                .filter(swap -> swap.getResult() == null || !"failed".equalsIgnoreCase(swap.getResult()))
                .count();

        return new DashboardSnapshot.Overview(
                totalVehicles,
                upcomingReservations,
                completedSwaps,
                totalPaid,
                unreadNotifications,
                nextReservation,
                lastPayment
        );
    }
}
