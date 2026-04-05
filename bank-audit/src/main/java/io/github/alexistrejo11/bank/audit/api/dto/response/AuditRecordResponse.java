package io.github.alexistrejo11.bank.audit.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AuditRecordResponse(
		UUID id,
		String eventType,
		UUID actorId,
		String entityType,
		UUID entityId,
		String payload,
		Instant createdAt
) {
}
