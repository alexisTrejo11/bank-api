package io.github.alexistrejo11.bank.notifications.application.handler.query;

import io.github.alexistrejo11.bank.notifications.domain.model.NotificationSummaryModel;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationLogRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetNotificationSummaryHandler {

	private final NotificationLogRepository notificationLogRepository;

	public GetNotificationSummaryHandler(NotificationLogRepository notificationLogRepository) {
		this.notificationLogRepository = notificationLogRepository;
	}

	@Transactional(readOnly = true)
	public NotificationSummaryModel handle() {
		return notificationLogRepository.summaryByStatus();
	}
}
