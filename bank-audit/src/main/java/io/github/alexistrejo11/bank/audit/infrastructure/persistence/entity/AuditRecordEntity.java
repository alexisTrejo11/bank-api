package io.github.alexistrejo11.bank.audit.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_records")
public class AuditRecordEntity {

	@Id
	@Column(nullable = false)
	private UUID id;

	@Column(name = "event_type", nullable = false, length = 512)
	private String eventType;

	@Column(name = "actor_id")
	private UUID actorId;

	@Column(name = "entity_type", nullable = false, length = 256)
	private String entityType;

	@Column(name = "entity_id")
	private UUID entityId;

	@Lob
	@Column(nullable = false)
	private String payload;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected AuditRecordEntity() {
	}

	public AuditRecordEntity(
			UUID id,
			String eventType,
			UUID actorId,
			String entityType,
			UUID entityId,
			String payload,
			Instant createdAt
	) {
		this.id = id;
		this.eventType = eventType;
		this.actorId = actorId;
		this.entityType = entityType;
		this.entityId = entityId;
		this.payload = payload;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public String getEventType() {
		return eventType;
	}

	public UUID getActorId() {
		return actorId;
	}

	public String getEntityType() {
		return entityType;
	}

	public UUID getEntityId() {
		return entityId;
	}

	public String getPayload() {
		return payload;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
