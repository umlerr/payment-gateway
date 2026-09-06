package umlerr.servicepayment.dto;

import lombok.Builder;
import lombok.Getter;
import umlerr.common.enums.PaymentMethod;
import umlerr.servicepayment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {

    private UUID id;
    private String userId;
    private String recipientId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod method;
    private PaymentStatus status;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
