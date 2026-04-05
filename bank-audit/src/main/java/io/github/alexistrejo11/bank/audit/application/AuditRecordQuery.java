package io.github.alexistrejo11.bank.audit.application;

import java.time.Instant;
import java.util.UUID;

public record AuditRecordQuery(
		String eventType,
		UUID actorId,
		String entityType,
		UUID entityId,
		Instant from,
		Instant to
) {
}
