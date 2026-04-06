CREATE TABLE loans (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    checking_account_id UUID NOT NULL,
    loan_account_id UUID NULL,
    principal DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    monthly_interest_rate DECIMAL(19, 8) NOT NULL,
    term_months INT NOT NULL,
    monthly_payment DECIMAL(19, 4) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_loans_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_loans_checking FOREIGN KEY (checking_account_id) REFERENCES accounts (id)
);

CREATE TABLE loan_repayments (
    id UUID NOT NULL PRIMARY KEY,
    loan_id UUID NOT NULL,
    installment_number INT NOT NULL,
    due_date DATE NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    status VARCHAR(32) NOT NULL,
    paid_at TIMESTAMP NULL,
    CONSTRAINT fk_loan_repayments_loan FOREIGN KEY (loan_id) REFERENCES loans (id) ON DELETE CASCADE,
    CONSTRAINT uq_loan_repayments_installment UNIQUE (loan_id, installment_number)
);

CREATE INDEX idx_loans_user ON loans (user_id);
