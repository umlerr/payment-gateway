# Payment Gateway

Пет-проект: платёжный шлюз на микросервисах — создание платежа, статусная машина
`CREATED → PROCESSING → COMPLETED/FAILED`, идемпотентность, вебхуки от банка и событийная
интеграция через Kafka.

> Work in progress — финальная документация появится к концу дня.

## Архитектура

| Сервис | Порт | Контекст | Ответственность |
|---|---|---|---|
| service-payment | 8080 | /api/payment/v1 | создание платежа, идемпотентность, вебхук банка, статусы |
| service-notification | 8081 | /api/notification/v1 | консюмер ивентов, мок-отправка уведомлений |
| service-analytics | 8082 | /api/analytics/v1 | агрегация: суммы по статусам/методам, топ отправителей |

Инфраструктура: Kafka (KRaft, без ZooKeeper) + PostgreSQL 18 x3 (database-per-service).

## Стек

- Java 25 LTS
- Spring Boot 4.1 (Spring Framework 7, Jakarta EE 11, Jackson 3)
- Gradle 9.7 (multi-project монорепо, toolchains)
- PostgreSQL 18 + Liquibase
- Apache Kafka 4.0 (KRaft)
- Lombok, springdoc-openapi v3

## Git-конвенция

- Коммиты: `feat:` / `fix:` / `chore:` / `docs:` / `infra:` / `test:` / `refactor:` — английский, императив
- Ветки: `feature/<slug>`, `fix/<slug>`, `chore/<slug>` от `main`, слияние через PR
