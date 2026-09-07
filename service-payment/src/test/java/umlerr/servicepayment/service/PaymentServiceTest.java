package umlerr.servicepayment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import umlerr.common.enums.PaymentMethod;
import umlerr.servicepayment.dto.BankWebhookRequest;
import umlerr.servicepayment.dto.PaymentCreateRequest;
import umlerr.servicepayment.dto.PaymentCreateResult;
import umlerr.servicepayment.dto.PaymentResponse;
import umlerr.servicepayment.enums.PaymentResult;
import umlerr.servicepayment.enums.PaymentStatus;
import umlerr.servicepayment.exception.NotFoundException;
import umlerr.servicepayment.kafka.PaymentEventPublisher;
import umlerr.servicepayment.model.Payment;
import umlerr.servicepayment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, paymentEventPublisher);
    }

    @Test
    void createPublishesEventAndReturnsCreated() {
        PaymentCreateRequest request = PaymentCreateRequest.builder()
            .userId("user-1")
            .recipientId("merchant-1")
            .amount(BigDecimal.valueOf(100.50))
            .currency("USD")
            .method(PaymentMethod.CARD)
            .build();

        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentCreateResult result = paymentService.create("key-1", request);

        assertThat(result.created()).isTrue();
        assertThat(result.payment().getStatus()).isEqualTo(PaymentStatus.CREATED);
        verify(paymentEventPublisher).publishCreated(any(Payment.class));
    }

    @Test
    void createExistingIdempotencyKeyReturnsExistingWithoutPublishing() {
        Payment existing = payment(PaymentStatus.CREATED);
        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(existing));

        PaymentCreateResult result = paymentService.create("key-1", request());

        assertThat(result.created()).isFalse();
        verify(paymentEventPublisher, never()).publishCreated(any(Payment.class));
    }

    @Test
    void getReturnsPaymentWhenFound() {
        Payment payment = payment(PaymentStatus.CREATED);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.get(payment.getId());

        assertThat(response.getId()).isEqualTo(payment.getId());
    }

    @Test
    void getThrowsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(paymentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.get(id))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void handleBankResultCompletesProcessingPayment() {
        Payment payment = payment(PaymentStatus.PROCESSING);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        paymentService.handleBankResult(payment.getId(), webhook(PaymentResult.COMPLETED));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentRepository).save(payment);
        verify(paymentEventPublisher).publishCompleted(payment);
        verify(paymentEventPublisher, never()).publishFailed(any());
    }

    @Test
    void handleBankResultFailsProcessingPayment() {
        Payment payment = payment(PaymentStatus.PROCESSING);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        paymentService.handleBankResult(payment.getId(), webhook(PaymentResult.FAILED));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(paymentEventPublisher).publishFailed(payment);
    }

    @Test
    void handleBankResultIgnoresNonProcessingPayment() {
        Payment payment = payment(PaymentStatus.CREATED);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        paymentService.handleBankResult(payment.getId(), webhook(PaymentResult.COMPLETED));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CREATED);
        verify(paymentEventPublisher, never()).publishCompleted(any());
        verify(paymentEventPublisher, never()).publishFailed(any());
    }

    @Test
    void handleBankResultThrowsWhenPaymentMissing() {
        UUID id = UUID.randomUUID();
        when(paymentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.handleBankResult(id, webhook(PaymentResult.COMPLETED)))
            .isInstanceOf(NotFoundException.class);
    }

    private Payment payment(PaymentStatus status) {
        return Payment.builder()
            .id(UUID.randomUUID())
            .userId("user-1")
            .recipientId("merchant-1")
            .amount(BigDecimal.valueOf(100.50))
            .currency("USD")
            .method(PaymentMethod.CARD)
            .status(status)
            .idempotencyKey("key-1")
            .build();
    }

    private PaymentCreateRequest request() {
        return PaymentCreateRequest.builder()
            .userId("user-1")
            .recipientId("merchant-1")
            .amount(BigDecimal.valueOf(100.50))
            .currency("USD")
            .method(PaymentMethod.CARD)
            .build();
    }

    private BankWebhookRequest webhook(PaymentResult result) {
        return BankWebhookRequest.builder()
            .paymentId(UUID.randomUUID())
            .result(result)
            .build();
    }
}
