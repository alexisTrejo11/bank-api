package io.github.alexistrejo11.bank.notifications.infrastructure.messaging;

import io.github.alexistrejo11.bank.notifications.domain.command.DispatchNotificationCommand;
import io.github.alexistrejo11.bank.notifications.domain.model.GenericEmailContent;
import java.util.Map;
import java.util.UUID;

/**
 * JSON DTO for {@code bank.notifications.dispatch} Kafka topic (schema v1).
 */
public record NotificationDispatchMessage(
		UUID userId,
		String sourceEventType,
		GenericEmailContent content,
		Map<String, Object> metadata
) {

	public DispatchNotificationCommand toCommand() {
		return new DispatchNotificationCommand(userId, sourceEventType, content, metadata);
	}

	public static NotificationDispatchMessage from(DispatchNotificationCommand c) {
		return new NotificationDispatchMessage(c.userId(), c.sourceEventType(), c.content(), c.metadata());
	}
}
