package db.migration;

import java.sql.Connection;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V5__CreateAuditRecords extends BaseJavaMigration {

	@Override
	public void migrate(Context context) throws Exception {
		Connection c = context.getConnection();
		String product = c.getMetaData().getDatabaseProductName();
		String payloadType = "H2".equalsIgnoreCase(product) ? "CLOB" : "TEXT";
		try (Statement st = c.createStatement()) {
			st.execute("""
					CREATE TABLE audit_records (
					    id UUID NOT NULL PRIMARY KEY,
					    event_type VARCHAR(512) NOT NULL,
					    actor_id UUID NULL,
					    entity_type VARCHAR(256) NOT NULL,
					    entity_id UUID NULL,
					    payload %s NOT NULL,
					    created_at TIMESTAMP NOT NULL
					)
					""".formatted(payloadType));
			st.execute("CREATE INDEX idx_audit_event_type ON audit_records (event_type)");
			st.execute("CREATE INDEX idx_audit_actor_id ON audit_records (actor_id)");
			st.execute("CREATE INDEX idx_audit_entity ON audit_records (entity_type, entity_id)");
			st.execute("CREATE INDEX idx_audit_created_at ON audit_records (created_at)");
		}
	}
}
