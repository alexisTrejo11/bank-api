package io.github.alexistrejo11.bank.audit.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Immutable audit event row.")
public record AuditRecordResponse(
		@Schema(description = "Record id") UUID id,
		@Schema(description = "Domain event type") String eventType,
		@Schema(description = "User id when applicable") UUID actorId,
		@Schema(description = "Aggregate or entity type") String entityType,
		@Schema(description = "Entity id when applicable") UUID entityId,
		@Schema(description = "JSON payload snapshot") String payload,
		@Schema(description = "Insert time (UTC)") Instant createdAt
) {
}
