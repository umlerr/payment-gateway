# payment-gateway

Пет-проект: платежный шлюз на микросервисах. Платеж проходит статусы
CREATED -> PROCESSING -> COMPLETED/FAILED, события уходят в Kafka.

## Стек

- Java 25, Spring Boot 4.1
- Gradle 9.7, Liquibase, Lombok
- PostgreSQL 18, по базе на сервис
- Kafka 4.0 (KRaft, без ZooKeeper)
- Checkstyle + PMD

Изменения в стеке, которые важно учесть при работе с проектом:

- **Spring Boot 4** использует Jackson 3 (`tools.jackson`), а starter для web
  переименован в `spring-boot-starter-webmvc`. Jackson-2 классы вроде
  `com.fasterxml.jackson.databind` недоступны - сериализация событий Kafka
  выполняется вручную через `ObjectMapper`.
- **Kafka 4** работает в режиме KRaft и не требует ZooKeeper.
- **Gradle 9** + **Java 25**: JDK подтягивается через gradle toolchains.

## Архитектура

| Сервис | Порт | База | Что делает |
|---|---|---|---|
| service-payment | 8080 | payment_db | прием платежей, идемпотентность, вебхук банка, симулятор банка |
| service-notification | 8081 | notification_db | читает события, шлет уведомления (мок) |
| service-analytics | 8082 | analytics_db | статистика по платежам |

События платежей публикуются в Kafka-топик `payments.events.v1`.
Консьюмеры: service-notification (группа `notification-service`) и
service-analytics (группа `analytics-service`).

## Запуск

Нужен Docker. JDK подтянется сам через gradle toolchains.

1. Поднять инфраструктуру (postgres x3, kafka, kafka-ui):

```
docker compose up -d
```

2. Запустить сервисы, каждый в своем терминале:

```
./gradlew :service-payment:bootRun      # 8080
./gradlew :service-notification:bootRun # 8081
./gradlew :service-analytics:bootRun    # 8082
```

3. Kafka UI на `http://localhost:8088`.

## Демо: от создания платежа до COMPLETED

1. Создать платеж (201 Created):

```
curl -s -H "X-Api-Key: payment-local-key" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: demo-1" \
  -d '{"userId":"user-1","recipientId":"merchant-1","amount":100.50,"currency":"USD","method":"CARD"}' \
  http://localhost:8080/api/payment/v1/payments
```

Запомнить `id` из ответа. Платеж создается со статусом CREATED и публикует
событие `PAYMENT_CREATED` в Kafka.

2. Дождаться ~6 секунд. Симулятор банка в service-payment переводит платеж
   через PROCESSING в финальный статус (COMPLETED с вероятностью 85%,
   иначе FAILED):

```
curl -s -H "X-Api-Key: payment-local-key" http://localhost:8080/api/payment/v1/payments/<id>
```

3. Проверить цепочку по записям:

- уведомления: `GET http://localhost:8081/api/notification/v1/notifications?paymentId=<id>`
- агрегаты: `GET http://localhost:8082/api/analytics/v1/summary`

4. Опционально: платеж можно завершить вручную через вебхук банка вместо
   симулятора (перевести статус в PROCESSING, затем):

```
curl -s -X POST -H "X-Webhook-Secret: bank-local-secret" \
  -H "Content-Type: application/json" \
  -d '{"paymentId":"<id>","result":"COMPLETED"}' \
  http://localhost:8080/api/payment/v1/webhooks/bank
```

## Проверка статусов в БД

```
docker exec -it postgres-payment psql -U postgres -d payment_db -c "select id,status from payments;"
docker exec -it postgres-notification psql -U postgres -d notification_db -c "select * from notifications;"
docker exec -it postgres-analytics psql -U postgres -d analytics_db -c "select * from payment_projections;"
```

## Разработка

- сборка и линтеры: `./gradlew build`
- ветки: feature/*, fix/*, chore/* от master, слияние через PR
- ключи/пароли задаются через переменные окружения (см. application.yaml),
  для локального запуска работают значения по умолчанию
