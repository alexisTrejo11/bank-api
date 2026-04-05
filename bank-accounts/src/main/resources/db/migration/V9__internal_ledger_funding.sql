-- System user + internal USD account used as the contra leg for loan disbursements and repayments (double-entry).

INSERT INTO users (id, email, password_hash, status, created_at, updated_at) VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'ledger.internal@bank', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO accounts (id, user_id, type, currency, status, created_at, updated_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'INTERNAL', 'USD', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
