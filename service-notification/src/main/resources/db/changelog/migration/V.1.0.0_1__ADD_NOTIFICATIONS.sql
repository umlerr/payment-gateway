--changeset umlerr:1.0.0_1__ADD_NOTIFICATIONS
CREATE TABLE notifications (
                               id          UUID PRIMARY KEY,
                               payment_id  UUID NOT NULL,
                               event_type  VARCHAR(32) NOT NULL,
                               channel     VARCHAR(32) NOT NULL,
                               recipient   VARCHAR(64) NOT NULL,
                               status      VARCHAR(16) NOT NULL,
                               payload     VARCHAR(2048),
                               created_at  TIMESTAMP NOT NULL
);
