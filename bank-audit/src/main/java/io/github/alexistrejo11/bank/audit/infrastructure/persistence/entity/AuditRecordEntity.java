package io.github.alexistrejo11.bank.audit.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.shared.shared_kernel.persistence.JpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_records")
public class AuditRecordEntity extends JpaEntity {

	@Column(name = "event_type", nullable = false, length = 512)
	private String eventType;

	@Column(name = "actor_id")
	private UUID actorId;

	@Column(name = "entity_type", nullable = false, length = 256)
	private String entityType;

	@Column(name = "entity_id")
	private UUID entityId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	protected AuditRecordEntity() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private UUID id;
		private String eventType;
		private UUID actorId;
		private String entityType;
		private UUID entityId;
		private String payload;
		private Instant createdAt;

		public Builder id(UUID id) {
			this.id = id;
			return this;
		}

		public Builder eventType(String eventType) {
			this.eventType = eventType;
			return this;
		}

		public Builder actorId(UUID actorId) {
			this.actorId = actorId;
			return this;
		}

		public Builder entityType(String entityType) {
			this.entityType = entityType;
			return this;
		}

		public Builder entityId(UUID entityId) {
			this.entityId = entityId;
			return this;
		}

		public Builder payload(String payload) {
			this.payload = payload;
			return this;
		}

		public Builder createdAt(Instant createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public AuditRecordEntity build() {
			AuditRecordEntity e = new AuditRecordEntity();
			e.id = id;
			e.eventType = eventType;
			e.actorId = actorId;
			e.entityType = entityType;
			e.entityId = entityId;
			e.payload = payload;
			e.createdAt = createdAt;
			e.updatedAt = createdAt;
			return e;
		}
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
}
