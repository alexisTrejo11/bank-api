package io.github.alexistrejo11.bank.notifications.infrastructure.messaging;

import io.github.alexistrejo11.bank.notifications.application.handler.command.DispatchNotificationHandler;
import io.github.alexistrejo11.bank.notifications.domain.command.DispatchNotificationCommand;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.notifications.dispatch-mode", havingValue = "direct", matchIfMissing = true)
public class HandlerNotificationDispatchIngress implements NotificationDispatchIngress {

	private final DispatchNotificationHandler dispatchNotificationHandler;

	public HandlerNotificationDispatchIngress(DispatchNotificationHandler dispatchNotificationHandler) {
		this.dispatchNotificationHandler = dispatchNotificationHandler;
	}

	@Override
	public void submit(DispatchNotificationCommand command) {
		dispatchNotificationHandler.handle(command);
	}
}
