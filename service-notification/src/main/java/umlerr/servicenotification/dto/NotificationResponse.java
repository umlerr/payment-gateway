package umlerr.servicenotification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

    @NotNull
    private final UUID id;

    @NotNull
    private final UUID paymentId;

    @NotBlank
    private final String eventType;

    @NotBlank
    private final String channel;

    @NotBlank
    private final String recipient;

    @NotBlank
    private final String status;

    private final String payload;

    @NotNull
    private final LocalDateTime createdAt;
}
