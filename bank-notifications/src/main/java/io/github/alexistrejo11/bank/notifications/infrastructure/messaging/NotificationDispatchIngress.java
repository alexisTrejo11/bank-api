package io.github.alexistrejo11.bank.notifications.infrastructure.messaging;

import io.github.alexistrejo11.bank.notifications.domain.command.DispatchNotificationCommand;

/**
 * Routes domain notification work either in-process or via Kafka (see {@code bank.notifications.dispatch-mode}).
 */
public interface NotificationDispatchIngress {

	void submit(DispatchNotificationCommand command);
}
