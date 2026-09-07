package umlerr.servicenotification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umlerr.common.enums.PaymentMethod;
import umlerr.common.event.PaymentEvent;
import umlerr.servicenotification.dto.NotificationResponse;
import umlerr.servicenotification.enums.NotificationChannel;
import umlerr.servicenotification.model.Notification;
import umlerr.servicenotification.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String STATUS_SENT = "SENT";

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> getAll(UUID paymentId) {
        List<Notification> notifications;
        if (paymentId != null) {
            notifications = notificationRepository.findByPaymentId(paymentId);
        } else {
            notifications = notificationRepository.findAll();
        }
        return notifications.stream().map(this::toResponse).toList();
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .paymentId(notification.getPaymentId())
            .eventType(notification.getEventType())
            .channel(notification.getChannel())
            .recipient(notification.getRecipient())
            .status(notification.getStatus())
            .payload(notification.getPayload())
            .createdAt(notification.getCreatedAt())
            .build();
    }

    @Transactional
    public void process(PaymentEvent event) {
        NotificationChannel channel = channelFor(event.method());
        log.info("mock send via {} to {}: payment {} {}", channel, event.userId(), event.paymentId(), event.type());
        notificationRepository.save(Notification.builder()
            .paymentId(event.paymentId())
            .eventType(event.type().name())
            .channel(channel.name())
            .recipient(event.userId())
            .status(STATUS_SENT)
            .payload("")
            .createdAt(LocalDateTime.now())
            .build());
    }

    private NotificationChannel channelFor(PaymentMethod method) {
        return switch (method) {
            case CARD -> NotificationChannel.PUSH;
            case SBP -> NotificationChannel.SMS;
            case TRANSFER -> NotificationChannel.EMAIL;
        };
    }
}
