--changeset umlerr:1.0.0_1__ADD_PAYMENTS
CREATE TABLE payments (
                          id              UUID PRIMARY KEY,
                          user_id         VARCHAR(255) NOT NULL,
                          recipient_id    VARCHAR(255) NOT NULL,
                          amount          NUMERIC(19, 2) NOT NULL,
                          currency        VARCHAR(3) NOT NULL,
                          method          VARCHAR(16) NOT NULL,
                          status          VARCHAR(16) NOT NULL,
                          idempotency_key VARCHAR(255) NOT NULL,
                          failure_reason  VARCHAR(512),
                          created_at      TIMESTAMP NOT NULL,
                          updated_at      TIMESTAMP NOT NULL,
                          CONSTRAINT unique_idempotency_key UNIQUE (idempotency_key)
);
