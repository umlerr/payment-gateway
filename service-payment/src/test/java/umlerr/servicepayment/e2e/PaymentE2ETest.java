package umlerr.servicepayment.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Сквозной smoke-тест против реально поднятых сервисов (docker compose + bootRun).
 * Запускать только когда инфраструктура и сервисы работают: ./gradlew :service-payment:test --tests '*E2E*'
 */
@Tag("e2e")
class PaymentE2ETest {

    private static final String PAYMENT_BASE = "http://localhost:8080/api/payment/v1";
    private static final String NOTIFICATION_BASE = "http://localhost:8081/api/notification/v1";
    private static final String ANALYTICS_BASE = "http://localhost:8082/api/analytics/v1";
    private static final String API_KEY = "payment-local-key";

    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void paymentFullLifecycle() throws Exception {
        String idempotencyKey = "e2e-" + UUID.randomUUID();

        // 1. create payment
        String body = "{\"userId\":\"user-e2e\",\"recipientId\":\"merchant-1\",\"amount\":77.77,"
            + "\"currency\":\"USD\",\"method\":\"CARD\"}";
        HttpResponse<String> created = post(PAYMENT_BASE + "/payments", body, idempotencyKey);
        assertThat(created.statusCode()).isEqualTo(201);
        JsonNode createdJson = mapper.readTree(created.body());
        UUID paymentId = UUID.fromString(createdJson.get("id").asText());
        assertThat(createdJson.get("status").asText()).isEqualTo("CREATED");

        // 2. idempotency: repeat with same key returns 200 and same id
        HttpResponse<String> repeated = post(PAYMENT_BASE + "/payments", body, idempotencyKey);
        assertThat(repeated.statusCode()).isEqualTo(200);
        JsonNode repeatedJson = mapper.readTree(repeated.body());
        assertThat(UUID.fromString(repeatedJson.get("id").asText())).isEqualTo(paymentId);

        // 3. invalid api key -> 401
        HttpResponse<String> unauthorized = get(PAYMENT_BASE + "/payments/" + paymentId, "wrong-key");
        assertThat(unauthorized.statusCode()).isEqualTo(401);

        // 4. wait for simulator to reach terminal state
        String status = "CREATED";
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline
            && ("CREATED".equals(status) || "PROCESSING".equals(status))) {
            HttpResponse<String> fetched = get(PAYMENT_BASE + "/payments/" + paymentId, API_KEY);
            status = mapper.readTree(fetched.body()).get("status").asText();
            if ("CREATED".equals(status) || "PROCESSING".equals(status)) {
                Thread.sleep(1000);
            }
        }
        assertThat(status).isIn("COMPLETED", "FAILED");

        // 5. notification exists for payment
        HttpResponse<String> notifications = get(NOTIFICATION_BASE + "/notifications?paymentId=" + paymentId, null);
        assertThat(notifications.statusCode()).isEqualTo(200);
        JsonNode notifArray = mapper.readTree(notifications.body());
        assertThat(notifArray.isArray()).isTrue();
        assertThat(notifArray.size()).isGreaterThan(0);

        // 6. analytics summary includes the payment amount across the top user
        HttpResponse<String> summary = get(ANALYTICS_BASE + "/summary", null);
        assertThat(summary.statusCode()).isEqualTo(200);
        JsonNode topUsers = mapper.readTree(summary.body()).get("topUsers");
        BigDecimal total = BigDecimal.ZERO;
        for (JsonNode userNode : topUsers) {
            if ("user-e2e".equals(userNode.get("userId").asText())) {
                total = total.add(userNode.get("totalAmount").decimalValue());
            }
        }
        assertThat(total).isGreaterThanOrEqualTo(new BigDecimal("77.77"));
    }

    private HttpResponse<String> post(String url, String body, String idempotencyKey)
        throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", "application/json")
            .header("X-Api-Key", API_KEY)
            .POST(HttpRequest.BodyPublishers.ofString(body));
        if (idempotencyKey != null) {
            builder.header("Idempotency-Key", idempotencyKey);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String url, String apiKey) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).GET();
        if (apiKey != null) {
            builder.header("X-Api-Key", apiKey);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
