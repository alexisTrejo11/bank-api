package db.migration;

import java.sql.Connection;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V13__CreateNotifications extends BaseJavaMigration {

	@Override
	public void migrate(Context context) throws Exception {
		Connection c = context.getConnection();
		String product = c.getMetaData().getDatabaseProductName();
		String lob = "H2".equalsIgnoreCase(product) ? "CLOB" : "TEXT";
		try (Statement st = c.createStatement()) {
			st.execute("""
					CREATE TABLE notifications (
					    id UUID NOT NULL PRIMARY KEY,
					    user_id UUID NULL,
					    channel VARCHAR(16) NOT NULL,
					    template_key VARCHAR(64) NOT NULL,
					    status VARCHAR(24) NOT NULL,
					    source_event_type VARCHAR(128) NOT NULL,
					    subject VARCHAR(512) NULL,
					    body_html %s NULL,
					    recipient_hint VARCHAR(256) NULL,
					    metadata_json %s NULL,
					    error_message VARCHAR(1024) NULL,
					    created_at TIMESTAMP NOT NULL,
					    updated_at TIMESTAMP NOT NULL,
					    dispatched_at TIMESTAMP NULL,
					    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
					)
					""".formatted(lob, lob));
			st.execute("CREATE INDEX idx_notifications_user ON notifications (user_id)");
			st.execute("CREATE INDEX idx_notifications_status ON notifications (status)");
			st.execute("CREATE INDEX idx_notifications_channel ON notifications (channel)");
			st.execute("CREATE INDEX idx_notifications_created ON notifications (created_at DESC)");
		}
	}
}
