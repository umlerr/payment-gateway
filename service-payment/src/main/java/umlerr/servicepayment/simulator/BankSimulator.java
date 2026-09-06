package umlerr.servicepayment.simulator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import umlerr.servicepayment.dto.BankWebhookRequest;
import umlerr.servicepayment.enums.PaymentResult;
import umlerr.servicepayment.enums.PaymentStatus;
import umlerr.servicepayment.repository.PaymentRepository;
import umlerr.servicepayment.service.PaymentService;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankSimulator {

    private static final double COMPLETION_PROBABILITY = 0.85;
    private static final int TICK_DELAY_MS = 2000;
    private static final int START_AFTER_SECONDS = 2;

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Scheduled(fixedDelay = TICK_DELAY_MS, initialDelay = TICK_DELAY_MS)
    public void tick() {
        startProcessing();
        finalizeProcessing();
    }

    private void startProcessing() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(START_AFTER_SECONDS);
        paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.CREATED, threshold)
            .forEach(payment -> {
                payment.setStatus(PaymentStatus.PROCESSING);
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                log.info("simulator: payment {} CREATED -> PROCESSING", payment.getId());
            });
    }

    private void finalizeProcessing() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(START_AFTER_SECONDS);
        paymentRepository.findByStatusAndUpdatedAtBefore(PaymentStatus.PROCESSING, threshold)
            .forEach(payment -> {
                PaymentResult result = ThreadLocalRandom.current().nextDouble() < COMPLETION_PROBABILITY
                    ? PaymentResult.COMPLETED
                    : PaymentResult.FAILED;
                String reason = result == PaymentResult.FAILED ? "bank declined the payment" : null;
                paymentService.handleBankResult(payment.getId(), BankWebhookRequest.builder()
                    .paymentId(payment.getId())
                    .result(result)
                    .reason(reason)
                    .build());
                log.info("simulator: payment {} PROCESSING -> {}", payment.getId(), result);
            });
    }
}
