package io.github.alexistrejo11.bank.notifications.infrastructure.messaging;

import io.github.alexistrejo11.bank.notifications.application.command.DispatchNotificationCommand;

/**
 * Enqueues notification dispatch work on Kafka.
 */
public interface NotificationDispatchIngress {

	void submit(DispatchNotificationCommand command);
}
