package evswap.swp391to4.service;

import evswap.swp391to4.dto.SwapTransactionRequest;
import evswap.swp391to4.dto.SwapTransactionResponse;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.entity.SwapTransaction;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.SwapTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SwapTransactionService {

    private final SwapTransactionRepository swapTransactionRepository;
    private final ReservationRepository reservationRepository;
    private final StationRepository stationRepository;
    private final BatteryRepository batteryRepository;

    @Transactional
    public SwapTransactionResponse recordSwap(SwapTransactionRequest request) {
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation không tồn tại"));
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));

        Battery batteryOut = null;
        if (request.getBatteryOutId() != null) {
            batteryOut = batteryRepository.findById(request.getBatteryOutId())
                    .orElseThrow(() -> new IllegalArgumentException("Battery out không tồn tại"));
        }

        Battery batteryIn = null;
        if (request.getBatteryInId() != null) {
            batteryIn = batteryRepository.findById(request.getBatteryInId())
                    .orElseThrow(() -> new IllegalArgumentException("Battery in không tồn tại"));
        }

        swapTransactionRepository.findByReservationReservationId(reservation.getReservationId())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Swap đã được ghi nhận cho reservation này");
                });

        SwapTransaction transaction = SwapTransaction.builder()
                .reservation(reservation)
                .station(station)
                .batteryOut(batteryOut)
                .batteryIn(batteryIn)
                .swappedAt(Instant.now())
                .result(request.getResult())
                .build();

        SwapTransaction saved = swapTransactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SwapTransactionResponse getByReservation(Integer reservationId) {
        return swapTransactionRepository.findByReservationReservationId(reservationId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy swap cho reservation"));
    }

    @Transactional(readOnly = true)
    public List<SwapTransactionResponse> listByStation(Integer stationId) {
        return swapTransactionRepository.findByStationStationIdOrderBySwappedAtDesc(stationId).stream()
                .map(this::toResponse)
                .toList();
    }

    private SwapTransactionResponse toResponse(SwapTransaction transaction) {
        return SwapTransactionResponse.builder()
                .swapId(transaction.getSwapId())
                .reservationId(transaction.getReservation() != null ? transaction.getReservation().getReservationId() : null)
                .stationId(transaction.getStation() != null ? transaction.getStation().getStationId() : null)
                .stationName(transaction.getStation() != null ? transaction.getStation().getName() : null)
                .batteryOutId(transaction.getBatteryOut() != null ? transaction.getBatteryOut().getBatteryId() : null)
                .batteryInId(transaction.getBatteryIn() != null ? transaction.getBatteryIn().getBatteryId() : null)
                .swappedAt(transaction.getSwappedAt())
                .result(transaction.getResult())
                .build();
    }
}
