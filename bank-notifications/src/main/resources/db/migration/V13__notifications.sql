CREATE TABLE notifications (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NULL,
    channel VARCHAR(16) NOT NULL,
    template_key VARCHAR(64) NOT NULL,
    status VARCHAR(24) NOT NULL,
    source_event_type VARCHAR(128) NOT NULL,
    subject VARCHAR(512) NULL,
    body_html CLOB NULL,
    recipient_hint VARCHAR(256) NULL,
    metadata_json CLOB NULL,
    error_message VARCHAR(1024) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    dispatched_at TIMESTAMP NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_notifications_user ON notifications (user_id);
CREATE INDEX idx_notifications_status ON notifications (status);
CREATE INDEX idx_notifications_channel ON notifications (channel);
CREATE INDEX idx_notifications_created ON notifications (created_at DESC);
