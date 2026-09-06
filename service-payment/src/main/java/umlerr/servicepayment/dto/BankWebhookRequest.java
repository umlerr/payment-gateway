package umlerr.servicepayment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import umlerr.servicepayment.enums.PaymentResult;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankWebhookRequest {

    @NotNull
    private UUID paymentId;

    @NotNull
    private PaymentResult result;

    private String reason;
}
