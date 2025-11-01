package evswap.swp391to4.dto;

import java.util.List;
import java.util.Map;

public record SwapSimulatorResponse(
        List<SwapSimulatorReservationDto> reservations,
        Map<String, Long> batteryStateCounts,
        List<SwapSimulatorTransactionDto> recentTransactions
) {
}
