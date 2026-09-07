package umlerr.serviceanalytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umlerr.common.event.PaymentEvent;
import umlerr.serviceanalytics.model.PaymentProjection;
import umlerr.serviceanalytics.repository.PaymentProjectionRepository;

@Service
@RequiredArgsConstructor
public class PaymentProjectionService {

    private final PaymentProjectionRepository projectionRepository;

    @Transactional
    public void upsert(PaymentEvent event) {
        PaymentProjection projection = projectionRepository.findById(event.paymentId())
            .map(p -> {
                p.setStatus(event.type().name());
                p.setEventTime(event.occurredAt());
                return p;
            })
            .orElseGet(() -> PaymentProjection.builder()
                .paymentId(event.paymentId())
                .userId(event.userId())
                .amount(event.amount())
                .currency(event.currency())
                .method(event.method())
                .status(event.type().name())
                .eventTime(event.occurredAt())
                .build());
        projectionRepository.save(projection);
    }
}
