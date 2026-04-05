package io.github.alexistrejo11.bank.notifications.domain.command;

import io.github.alexistrejo11.bank.notifications.domain.model.GenericEmailContent;
import java.util.Map;
import java.util.UUID;

public record DispatchNotificationCommand(
		UUID userId,
		String sourceEventType,
		GenericEmailContent content,
		Map<String, Object> metadata
) {
}
