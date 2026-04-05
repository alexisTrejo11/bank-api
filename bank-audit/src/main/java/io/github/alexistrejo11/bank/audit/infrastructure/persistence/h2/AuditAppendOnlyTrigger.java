package io.github.alexistrejo11.bank.audit.infrastructure.persistence.h2;

import java.sql.Connection;
import java.sql.SQLException;
import org.h2.api.Trigger;

/**
 * H2 row trigger: rejects any UPDATE or DELETE on {@code audit_records}. PostgreSQL should use an equivalent SQL trigger.
 */
public class AuditAppendOnlyTrigger implements Trigger {

	@Override
	public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) {
	}

	@Override
	public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
		throw new SQLException("audit_records is append-only", "45000");
	}

	@Override
	public void close() {
	}
}
