package io.github.alexistrejo11.bank.notifications.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.notifications.domain.model.NotificationChannel;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationStatus;
import io.github.alexistrejo11.bank.shared.shared_kernel.persistence.JpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity extends JpaEntity {

	@Column(name = "user_id")
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private NotificationChannel channel;

	@Column(name = "template_key", nullable = false, length = 64)
	private String templateKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private NotificationStatus status;

	@Column(name = "source_event_type", nullable = false, length = 128)
	private String sourceEventType;

	@Column(length = 512)
	private String subject;

	@Column(name = "body_html", columnDefinition = "TEXT")
	private String bodyHtml;

	@Column(name = "recipient_hint", length = 256)
	private String recipientHint;

	@Column(name = "metadata_json", columnDefinition = "TEXT")
	private String metadataJson;

	@Column(name = "error_message", length = 1024)
	private String errorMessage;

	@Column(name = "dispatched_at")
	private Instant dispatchedAt;

	protected NotificationEntity() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private UUID id;
		private UUID userId;
		private NotificationChannel channel;
		private String templateKey;
		private NotificationStatus status;
		private String sourceEventType;
		private String subject;
		private String bodyHtml;
		private String recipientHint;
		private String metadataJson;
		private String errorMessage;
		private Instant createdAt;
		private Instant updatedAt;
		private Instant dispatchedAt;

		public Builder id(UUID id) {
			this.id = id;
			return this;
		}

		public Builder userId(UUID userId) {
			this.userId = userId;
			return this;
		}

		public Builder channel(NotificationChannel channel) {
			this.channel = channel;
			return this;
		}

		public Builder templateKey(String templateKey) {
			this.templateKey = templateKey;
			return this;
		}

		public Builder status(NotificationStatus status) {
			this.status = status;
			return this;
		}

		public Builder sourceEventType(String sourceEventType) {
			this.sourceEventType = sourceEventType;
			return this;
		}

		public Builder subject(String subject) {
			this.subject = subject;
			return this;
		}

		public Builder bodyHtml(String bodyHtml) {
			this.bodyHtml = bodyHtml;
			return this;
		}

		public Builder recipientHint(String recipientHint) {
			this.recipientHint = recipientHint;
			return this;
		}

		public Builder metadataJson(String metadataJson) {
			this.metadataJson = metadataJson;
			return this;
		}

		public Builder errorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		public Builder createdAt(Instant createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder updatedAt(Instant updatedAt) {
			this.updatedAt = updatedAt;
			return this;
		}

		public Builder dispatchedAt(Instant dispatchedAt) {
			this.dispatchedAt = dispatchedAt;
			return this;
		}

		public NotificationEntity build() {
			NotificationEntity e = new NotificationEntity();
			e.id = id;
			e.userId = userId;
			e.channel = channel;
			e.templateKey = templateKey;
			e.status = status;
			e.sourceEventType = sourceEventType;
			e.subject = subject;
			e.bodyHtml = bodyHtml;
			e.recipientHint = recipientHint;
			e.metadataJson = metadataJson;
			e.errorMessage = errorMessage;
			e.createdAt = createdAt;
			e.updatedAt = updatedAt;
			e.dispatchedAt = dispatchedAt;
			return e;
		}
	}

	public UUID getUserId() {
		return userId;
	}

	public NotificationChannel getChannel() {
		return channel;
	}

	public String getTemplateKey() {
		return templateKey;
	}

	public NotificationStatus getStatus() {
		return status;
	}

	public void setStatus(NotificationStatus status) {
		this.status = status;
	}

	public String getSourceEventType() {
		return sourceEventType;
	}

	public String getSubject() {
		return subject;
	}

	public String getBodyHtml() {
		return bodyHtml;
	}

	public String getRecipientHint() {
		return recipientHint;
	}

	public String getMetadataJson() {
		return metadataJson;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Instant getDispatchedAt() {
		return dispatchedAt;
	}

	public void setDispatchedAt(Instant dispatchedAt) {
		this.dispatchedAt = dispatchedAt;
	}
}
