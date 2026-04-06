package db.migration;

import java.sql.Connection;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * H2: Java row trigger class on classpath. PostgreSQL: equivalent plpgsql trigger.
 */
public class V5_1__AuditAppendOnlyTrigger extends BaseJavaMigration {

	@Override
	public void migrate(Context context) throws Exception {
		Connection c = context.getConnection();
		String product = c.getMetaData().getDatabaseProductName();
		try (Statement st = c.createStatement()) {
			if ("H2".equalsIgnoreCase(product)) {
				st.execute(
						"CREATE TRIGGER audit_records_immutable BEFORE UPDATE, DELETE ON audit_records FOR EACH ROW "
								+ "CALL 'io.github.alexistrejo11.bank.audit.infrastructure.persistence.h2.AuditAppendOnlyTrigger'");
			}
			else if (product != null && product.toLowerCase().contains("postgresql")) {
				st.execute("""
						CREATE OR REPLACE FUNCTION audit_records_reject_mutation() RETURNS trigger AS $$
						BEGIN
						  RAISE EXCEPTION 'audit_records is append-only';
						END;
						$$ LANGUAGE plpgsql
						""");
				st.execute("""
						CREATE TRIGGER audit_records_immutable
						BEFORE UPDATE OR DELETE ON audit_records
						FOR EACH ROW EXECUTE PROCEDURE audit_records_reject_mutation()
						""");
			}
		}
	}
}
