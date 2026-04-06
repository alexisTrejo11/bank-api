CREATE TABLE transfers (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    source_account_id UUID NOT NULL,
    target_account_id UUID NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    idempotency_key UUID NOT NULL,
    failure_reason VARCHAR(1024) NULL,
    reference_transfer_id UUID NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_transfers_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_transfers_source FOREIGN KEY (source_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_transfers_target FOREIGN KEY (target_account_id) REFERENCES accounts (id),
    CONSTRAINT uq_transfers_user_idempotency UNIQUE (user_id, idempotency_key)
);

CREATE INDEX idx_transfers_reference ON transfers (reference_transfer_id);
