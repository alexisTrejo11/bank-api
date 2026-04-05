-- Audit log (H2 / PostgreSQL compatible DDL). Append-only enforced via H2 Java trigger (see CALL below).
-- For native PostgreSQL deployments, replace the trigger with a plpgsql BEFORE UPDATE OR DELETE trigger.

CREATE TABLE audit_records (
    id UUID NOT NULL PRIMARY KEY,
    event_type VARCHAR(512) NOT NULL,
    actor_id UUID NULL,
    entity_type VARCHAR(256) NOT NULL,
    entity_id UUID NULL,
    payload CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_event_type ON audit_records (event_type);
CREATE INDEX idx_audit_actor_id ON audit_records (actor_id);
CREATE INDEX idx_audit_entity ON audit_records (entity_type, entity_id);
CREATE INDEX idx_audit_created_at ON audit_records (created_at);

CREATE TRIGGER audit_records_immutable BEFORE UPDATE, DELETE ON audit_records FOR EACH ROW
    CALL 'io.github.alexistrejo11.bank.audit.infrastructure.persistence.h2.AuditAppendOnlyTrigger';
