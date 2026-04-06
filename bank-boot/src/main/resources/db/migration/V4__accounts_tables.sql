CREATE TABLE accounts (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE ledger_entries (
    id UUID NOT NULL PRIMARY KEY,
    account_id UUID NOT NULL,
    entry_type VARCHAR(8) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reference_type VARCHAR(64) NOT NULL,
    reference_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_ledger_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE INDEX idx_ledger_account ON ledger_entries (account_id);
CREATE INDEX idx_ledger_ref ON ledger_entries (reference_type, reference_id);
