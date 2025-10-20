package evswap.swp391to4.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record NotificationDto(
        Integer id,
        String title,
        String type,
        boolean read,
        Instant sentAt
) {
}
