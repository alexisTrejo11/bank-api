package io.github.alexistrejo11.bank.notifications.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationRecordResponse(
		UUID notificationId,
		UUID userId,
		String channel,
		String templateKey,
		String status,
		String sourceEventType,
		String subject,
		String recipientHint,
		Instant createdAt,
		Instant dispatchedAt,
		String errorMessage
) {
}
