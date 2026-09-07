package umlerr.serviceanalytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umlerr.common.enums.PaymentMethod;
import umlerr.common.event.PaymentEvent;
import umlerr.common.event.PaymentEventType;
import umlerr.serviceanalytics.model.PaymentProjection;
import umlerr.serviceanalytics.repository.PaymentProjectionRepository;

@ExtendWith(MockitoExtension.class)
class PaymentProjectionServiceTest {

    @Mock
    private PaymentProjectionRepository projectionRepository;

    private PaymentProjectionService projectionService;

    @BeforeEach
    void setUp() {
        projectionService = new PaymentProjectionService(projectionRepository);
    }

    @Test
    void upsertCreatesNewProjectionWhenNotExists() {
        PaymentEvent event = event(PaymentEventType.PAYMENT_CREATED, "user-1");
        when(projectionRepository.findById(event.paymentId())).thenReturn(Optional.empty());

        projectionService.upsert(event);

        ArgumentCaptor<PaymentProjection> captor = ArgumentCaptor.forClass(PaymentProjection.class);
        verify(projectionRepository).save(captor.capture());
        PaymentProjection saved = captor.getValue();
        assertThat(saved.getPaymentId()).isEqualTo(event.paymentId());
        assertThat(saved.getStatus()).isEqualTo("PAYMENT_CREATED");
        assertThat(saved.getMethod()).isEqualTo(PaymentMethod.CARD);
    }

    @Test
    void upsertUpdatesStatusWhenExists() {
        PaymentEvent event = event(PaymentEventType.PAYMENT_COMPLETED, "user-1");
        PaymentProjection existing = PaymentProjection.builder()
            .paymentId(event.paymentId())
            .userId("user-1")
            .amount(BigDecimal.valueOf(100.50))
            .currency("USD")
            .method(PaymentMethod.CARD)
            .status("PAYMENT_CREATED")
            .eventTime(Instant.now())
            .build();
        when(projectionRepository.findById(event.paymentId())).thenReturn(Optional.of(existing));

        projectionService.upsert(event);

        assertThat(existing.getStatus()).isEqualTo("PAYMENT_COMPLETED");
        assertThat(existing.getEventTime()).isEqualTo(event.occurredAt());
        verify(projectionRepository).save(existing);
    }

    private PaymentEvent event(PaymentEventType type, String userId) {
        return new PaymentEvent(
            type,
            UUID.randomUUID(),
            userId,
            BigDecimal.valueOf(100.50),
            "USD",
            PaymentMethod.CARD,
            null,
            Instant.now()
        );
    }
}
