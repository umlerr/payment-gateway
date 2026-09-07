package umlerr.servicenotification.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

    private final UUID id;

    private final UUID paymentId;

    private final String eventType;

    private final String channel;

    private final String recipient;

    private final String status;

    private final String payload;

    private final LocalDateTime createdAt;
}
