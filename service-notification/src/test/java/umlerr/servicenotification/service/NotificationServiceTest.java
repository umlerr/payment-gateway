package umlerr.servicenotification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
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
import umlerr.servicenotification.model.Notification;
import umlerr.servicenotification.repository.NotificationRepository;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void processCardMapsToPushChannel() {
        PaymentEvent event = event(PaymentMethod.CARD);

        notificationService.process(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getChannel()).isEqualTo("PUSH");
        assertThat(saved.getEventType()).isEqualTo("PAYMENT_CREATED");
        assertThat(saved.getRecipient()).isEqualTo("user-1");
        assertThat(saved.getStatus()).isEqualTo("SENT");
    }

    @Test
    void processSbpMapsToSmsChannel() {
        notificationService.process(event(PaymentMethod.SBP));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo("SMS");
    }

    @Test
    void processTransferMapsToEmailChannel() {
        notificationService.process(event(PaymentMethod.TRANSFER));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo("EMAIL");
    }

    @Test
    void getAllWithoutFilterReturnsAll() {
        Notification n = new Notification();
        when(notificationRepository.findAll()).thenReturn(List.of(n));

        var result = notificationService.getAll(null);

        assertThat(result).hasSize(1);
    }

    private PaymentEvent event(PaymentMethod method) {
        return new PaymentEvent(
            PaymentEventType.PAYMENT_CREATED,
            UUID.randomUUID(),
            "user-1",
            BigDecimal.valueOf(100.50),
            "USD",
            method,
            Instant.now()
        );
    }
}
