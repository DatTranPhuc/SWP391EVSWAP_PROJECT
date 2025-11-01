package evswap.swp391to4.dto;

import java.time.Instant;

public record SwapSimulatorTransactionDto(
        Integer swapId,
        Integer reservationId,
        Instant swappedAt,
        String result,
        String batteryOutModel,
        String batteryInModel
) {
}
