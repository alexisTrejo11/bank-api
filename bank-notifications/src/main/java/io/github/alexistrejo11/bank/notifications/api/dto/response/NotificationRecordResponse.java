package io.github.alexistrejo11.bank.notifications.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "One notification dispatch attempt.")
public record NotificationRecordResponse(
		@Schema(description = "Notification id") UUID notificationId,
		@Schema(description = "Target user when set") UUID userId,
		@Schema(description = "EMAIL, SMS, …") String channel,
		@Schema(description = "Template key") String templateKey,
		@Schema(description = "Dispatch status") String status,
		@Schema(description = "Upstream domain event") String sourceEventType,
		@Schema(description = "Subject line when email") String subject,
		@Schema(description = "Masked recipient") String recipientHint,
		@Schema(description = "Row created (UTC)") Instant createdAt,
		@Schema(description = "When provider accepted send") Instant dispatchedAt,
		@Schema(description = "Last error message") String errorMessage
) {
}
