# payment-gateway

Пет-проект: платежный шлюз на микросервисах. Платеж проходит статусы
CREATED -> PROCESSING -> COMPLETED/FAILED, события уходят в Kafka.

## Сервисы

| Сервис | Порт | Что делает |
|---|---|---|
| service-payment | 8080 | прием платежей, идемпотентность, вебхук банка |
| service-notification | 8081 | читает события, рассылает уведомления |
| service-analytics | 8082 | статистика по платежам |

## Стек

- Java 25, Spring Boot 4.1
- Gradle 9.7, Liquibase, Lombok
- PostgreSQL 18, по базе на сервис
- Kafka 4.0 (KRaft, без ZooKeeper)

## Запуск

Нужен Docker, JDK подтянется сам через gradle toolchains.

```
docker compose up -d
./gradlew :service-payment:bootRun
```

kafka-ui на localhost:8088

## Разработка

- сборка и линтеры: `./gradlew build`
- ветки: feature/*, fix/*, chore/* от master, слияние через PR
