--changeset umlerr:1.0.0_1__ADD_PAYMENT_PROJECTIONS
CREATE TABLE payment_projections (
                                     payment_id UUID PRIMARY KEY,
                                     user_id    VARCHAR(255) NOT NULL,
                                     amount     NUMERIC(19, 2) NOT NULL,
                                     currency   VARCHAR(3) NOT NULL,
                                     method     VARCHAR(16) NOT NULL,
                                     status     VARCHAR(32) NOT NULL,
                                     event_time TIMESTAMP NOT NULL
);
