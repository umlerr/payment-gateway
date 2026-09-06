package umlerr.servicepayment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import umlerr.servicepayment.dto.BankWebhookRequest;
import umlerr.servicepayment.service.PaymentService;

@RestController
@RequiredArgsConstructor
public class BankWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhooks/bank")
    public void handleBankWebhook(@Valid @RequestBody BankWebhookRequest request) {
        paymentService.handleBankResult(request.getPaymentId(), request);
    }
}
