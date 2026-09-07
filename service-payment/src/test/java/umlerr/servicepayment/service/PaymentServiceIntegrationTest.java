package umlerr.servicepayment.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import umlerr.common.enums.PaymentMethod;
import umlerr.servicepayment.dto.PaymentCreateRequest;
import umlerr.servicepayment.dto.PaymentCreateResult;
import umlerr.servicepayment.kafka.PaymentEventPublisher;

/**
 * Интеграционный тест на реальной PostgreSQL (Testcontainers).
 * Проверяет создание платежа и идемпотентность через реальный JPA-репозиторий.
 */
@Testcontainers
@SpringBootTest
class PaymentServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18")
        .withDatabaseName("payment_db_test")
        .withUsername("postgres")
        .withPassword("postgres");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("kafka.bootstrap-servers", () -> "localhost:9092");
    }

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private PaymentEventPublisher paymentEventPublisher;

    @Test
    void createThenRepeatWithSameKeyIsIdempotent() {
        PaymentCreateRequest request = PaymentCreateRequest.builder()
            .userId("user-1")
            .recipientId("merchant-1")
            .amount(BigDecimal.valueOf(100.50))
            .currency("USD")
            .method(PaymentMethod.CARD)
            .build();

        PaymentCreateResult first = paymentService.create("it-key-1", request);
        assertThat(first.created()).isTrue();
        assertThat(first.payment().getStatus().name()).isEqualTo("CREATED");

        PaymentCreateResult second = paymentService.create("it-key-1", request);
        assertThat(second.created()).isFalse();
        assertThat(second.payment().getId()).isEqualTo(first.payment().getId());
    }
}
