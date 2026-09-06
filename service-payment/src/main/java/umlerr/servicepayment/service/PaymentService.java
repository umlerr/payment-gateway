package umlerr.servicepayment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import umlerr.servicepayment.dto.PaymentCreateRequest;
import umlerr.servicepayment.dto.PaymentCreateResult;
import umlerr.servicepayment.dto.PaymentResponse;
import umlerr.servicepayment.enums.PaymentStatus;
import umlerr.servicepayment.exception.NotFoundException;
import umlerr.servicepayment.kafka.PaymentEventPublisher;
import umlerr.servicepayment.model.Payment;
import umlerr.servicepayment.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentCreateResult create(String idempotencyKey, PaymentCreateRequest request) {
        var existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return new PaymentCreateResult(toResponse(existing.get()), false);
        }

        var now = LocalDateTime.now();
        var payment = Payment.builder()
            .userId(request.getUserId())
            .recipientId(request.getRecipientId())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .method(request.getMethod())
            .status(PaymentStatus.CREATED)
            .idempotencyKey(idempotencyKey)
            .createdAt(now)
            .updatedAt(now)
            .build();

        try {
            var saved = paymentRepository.save(payment);
            paymentEventPublisher.publishCreated(saved);
            return new PaymentCreateResult(toResponse(saved), true);
        } catch (DataIntegrityViolationException e) {
            return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(p -> new PaymentCreateResult(toResponse(p), false))
                .orElseThrow(() -> new IllegalStateException("payment not found after idempotency conflict"));
        }
    }

    public PaymentResponse get(UUID id) {
        return paymentRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new NotFoundException("payment not found by id: " + id));
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .userId(payment.getUserId())
            .recipientId(payment.getRecipientId())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .method(payment.getMethod())
            .status(payment.getStatus())
            .idempotencyKey(payment.getIdempotencyKey())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .build();
    }
}
