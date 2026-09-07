package umlerr.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import umlerr.common.enums.PaymentMethod;

public record PaymentEvent(
    PaymentEventType type,
    UUID paymentId,
    String userId,
    BigDecimal amount,
    String currency,
    PaymentMethod method,
    Instant occurredAt
) {
}
