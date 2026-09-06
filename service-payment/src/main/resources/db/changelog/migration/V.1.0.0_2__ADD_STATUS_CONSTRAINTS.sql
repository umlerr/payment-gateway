--changeset umlerr:1.0.0_2__ADD_STATUS_CONSTRAINTS
ALTER TABLE payments
    ADD CONSTRAINT check_payment_status CHECK (status IN ('CREATED', 'PROCESSING', 'COMPLETED', 'FAILED'));
