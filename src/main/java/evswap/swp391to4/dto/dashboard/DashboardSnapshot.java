package evswap.swp391to4.dto.dashboard;

import evswap.swp391to4.dto.NotificationResponse;
import evswap.swp391to4.dto.PaymentResponse;
import evswap.swp391to4.dto.ReservationResponse;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.dto.SwapTransactionResponse;
import evswap.swp391to4.dto.TicketResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DashboardSnapshot(
        Overview overview,
        List<VehicleCard> vehicles,
        List<ReservationResponse> reservations,
        List<SwapTransactionResponse> swaps,
        List<PaymentResponse> payments,
        List<NotificationResponse> notifications,
        List<TicketResponse> tickets,
        List<StationResponse> stations
) {
    public record Overview(
            int totalVehicles,
            int upcomingReservations,
            int completedSwaps,
            BigDecimal totalPaid,
            long unreadNotifications,
            NextReservation nextReservation,
            LastPayment lastPayment
    ) {
        public record NextReservation(
                Integer reservationId,
                String stationName,
                Instant reservedStart,
                String status
        ) {
        }

        public record LastPayment(
                Integer paymentId,
                BigDecimal amount,
                String method,
                String status,
                Instant paidAt
        ) {
        }
    }

    public record VehicleCard(
            Integer vehicleId,
            String displayName,
            String vin,
            String plateNumber,
            String model,
            Instant createdAt
    ) {
    }
}
