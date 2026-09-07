package umlerr.serviceanalytics.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import umlerr.common.event.PaymentEvent;
import umlerr.serviceanalytics.service.PaymentProjectionService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentProjectionService projectionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.payment-topic}")
    public void onPaymentEvent(String message) {
        try {
            PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
            projectionService.upsert(event);
            log.info("projection upserted for payment {} event {}", event.paymentId(), event.type());
        } catch (Exception e) {
            log.error("failed to process payment event: {}", message, e);
        }
    }
}
