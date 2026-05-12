package io.github.alexistrejo11.bank.audit.application.query;

import java.time.Instant;
import java.util.UUID;

public record AuditRecordFilters(
		String eventType,
		UUID actorId,
		String entityType,
		UUID entityId,
		Instant from,
		Instant to
) {
}
