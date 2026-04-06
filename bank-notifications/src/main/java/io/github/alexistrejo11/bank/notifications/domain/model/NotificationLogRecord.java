package io.github.alexistrejo11.bank.notifications.domain.model;

import java.time.Instant;
import java.util.UUID;

public record NotificationLogRecord(
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
}
