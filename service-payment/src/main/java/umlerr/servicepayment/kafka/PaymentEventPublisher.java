package umlerr.servicepayment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import umlerr.common.event.PaymentEvent;
import umlerr.common.event.PaymentEventType;
import umlerr.servicepayment.model.Payment;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.payment-topic}")
    private String topic;

    public void publishCreated(Payment payment) {
        PaymentEvent event = new PaymentEvent(
            PaymentEventType.PAYMENT_CREATED,
            payment.getId(),
            payment.getUserId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getMethod(),
            null,
            Instant.now()
        );
        String json = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(topic, payment.getId().toString(), json);
        log.info("PAYMENT_CREATED published for payment {}", payment.getId());
    }

    public void publishCompleted(Payment payment) {
        publish(payment, PaymentEventType.PAYMENT_COMPLETED);
    }

    public void publishFailed(Payment payment) {
        publish(payment, PaymentEventType.PAYMENT_FAILED);
    }

    private void publish(Payment payment, PaymentEventType type) {
        PaymentEvent event = new PaymentEvent(
            type,
            payment.getId(),
            payment.getUserId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getMethod(),
            payment.getFailureReason(),
            Instant.now()
        );
        String json = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(topic, payment.getId().toString(), json);
        log.info("{} published for payment {}", type, payment.getId());
    }
}
