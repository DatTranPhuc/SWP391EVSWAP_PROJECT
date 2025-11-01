package evswap.swp391to4.service;

import evswap.swp391to4.dto.SwapSimulatorReservationDto;
import evswap.swp391to4.dto.SwapSimulatorResponse;
import evswap.swp391to4.dto.SwapSimulatorTransactionDto;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.entity.SwapTransaction;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.SwapTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SwapSimulatorService {

    private final ReservationRepository reservationRepository;
    private final SwapTransactionRepository swapTransactionRepository;
    private final BatteryService batteryService;

    @Transactional(readOnly = true)
    public SwapSimulatorResponse loadDataForStation(Station station) {
        List<Reservation> reservations = reservationRepository.findByStationStationId(station.getStationId());
        reservations.sort(Comparator.comparing(Reservation::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        List<SwapSimulatorReservationDto> reservationDtos = reservations.stream()
                .map(this::toReservationDto)
                .toList();

        Map<String, Long> batteryCounts = new LinkedHashMap<>();
        batteryCounts.put("full", batteryService.countBatteriesByState(station, "full"));
        batteryCounts.put("charging", batteryService.countBatteriesByState(station, "charging"));
        batteryCounts.put("maintenance", batteryService.countBatteriesByState(station, "maintenance"));
        batteryCounts.put("retired", batteryService.countBatteriesByState(station, "retired"));

        List<SwapTransaction> transactions = swapTransactionRepository
                .findTop5ByStationStationIdOrderBySwappedAtDesc(station.getStationId());
        List<SwapSimulatorTransactionDto> transactionDtos = transactions.stream()
                .map(this::toTransactionDto)
                .toList();

        return new SwapSimulatorResponse(reservationDtos, batteryCounts, transactionDtos);
    }

    private SwapSimulatorReservationDto toReservationDto(Reservation reservation) {
        String driverName = reservation.getDriver() != null ? reservation.getDriver().getFullName() : "-";
        String vehiclePlate = reservation.getVehicle() != null ? reservation.getVehicle().getPlateNumber() : null;
        Battery assignedBattery = reservation.getAssignedBattery();
        String batteryModel = assignedBattery != null ? assignedBattery.getModel() : null;

        return new SwapSimulatorReservationDto(
                reservation.getReservationId(),
                reservation.getStatus(),
                reservation.getReservedStart(),
                reservation.getCreatedAt(),
                driverName,
                vehiclePlate,
                batteryModel
        );
    }

    private SwapSimulatorTransactionDto toTransactionDto(SwapTransaction transaction) {
        String batteryOutModel = transaction.getBatteryOut() != null ? transaction.getBatteryOut().getModel() : null;
        String batteryInModel = transaction.getBatteryIn() != null ? transaction.getBatteryIn().getModel() : null;

        return new SwapSimulatorTransactionDto(
                transaction.getSwapId(),
                transaction.getReservation() != null ? transaction.getReservation().getReservationId() : null,
                transaction.getSwappedAt(),
                transaction.getResult(),
                batteryOutModel,
                batteryInModel
        );
    }
}
