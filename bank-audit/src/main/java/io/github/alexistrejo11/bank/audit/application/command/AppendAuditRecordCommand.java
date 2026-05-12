package io.github.alexistrejo11.bank.audit.application.command;

import java.time.Instant;
import java.util.UUID;

public record AppendAuditRecordCommand(
		UUID id,
		String eventType,
		UUID actorId,
		String entityType,
		UUID entityId,
		String payloadJson,
		Instant createdAt
) {
}
