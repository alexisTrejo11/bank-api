package io.github.alexistrejo11.bank.notifications.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.notifications.domain.model.NotificationChannel;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

	@Id
	private UUID id;

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

	@Lob
	@Column(name = "body_html")
	private String bodyHtml;

	@Column(name = "recipient_hint", length = 256)
	private String recipientHint;

	@Lob
	@Column(name = "metadata_json")
	private String metadataJson;

	@Column(name = "error_message", length = 1024)
	private String errorMessage;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "dispatched_at")
	private Instant dispatchedAt;

	protected NotificationEntity() {
	}

	public NotificationEntity(
			UUID id,
			UUID userId,
			NotificationChannel channel,
			String templateKey,
			NotificationStatus status,
			String sourceEventType,
			String subject,
			String bodyHtml,
			String recipientHint,
			String metadataJson,
			String errorMessage,
			Instant createdAt,
			Instant updatedAt,
			Instant dispatchedAt
	) {
		this.id = id;
		this.userId = userId;
		this.channel = channel;
		this.templateKey = templateKey;
		this.status = status;
		this.sourceEventType = sourceEventType;
		this.subject = subject;
		this.bodyHtml = bodyHtml;
		this.recipientHint = recipientHint;
		this.metadataJson = metadataJson;
		this.errorMessage = errorMessage;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.dispatchedAt = dispatchedAt;
	}

	public UUID getId() {
		return id;
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

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Instant getDispatchedAt() {
		return dispatchedAt;
	}

	public void setDispatchedAt(Instant dispatchedAt) {
		this.dispatchedAt = dispatchedAt;
	}
}
