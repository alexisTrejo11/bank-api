CREATE TABLE audit_records (
    id UUID NOT NULL PRIMARY KEY,
    event_type VARCHAR(512) NOT NULL,
    actor_id UUID NULL,
    entity_type VARCHAR(256) NOT NULL,
    entity_id UUID NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_event_type ON audit_records (event_type);
CREATE INDEX idx_audit_actor_id ON audit_records (actor_id);
CREATE INDEX idx_audit_entity ON audit_records (entity_type, entity_id);
CREATE INDEX idx_audit_created_at ON audit_records (created_at);
