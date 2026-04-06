package io.github.alexistrejo11.bank.audit.domain.model;

import java.time.Instant;
import java.util.UUID;

public record AuditRecord(
		UUID id,
		String eventType,
		UUID actorId,
		String entityType,
		UUID entityId,
		String payload,
		Instant createdAt
) {
}
