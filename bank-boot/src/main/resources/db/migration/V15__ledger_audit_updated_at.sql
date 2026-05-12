-- Align ledger and audit tables with shared JpaEntity (id + created_at + updated_at).

ALTER TABLE ledger_entries ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
UPDATE ledger_entries SET updated_at = created_at;

ALTER TABLE audit_records ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
UPDATE audit_records SET updated_at = created_at;
