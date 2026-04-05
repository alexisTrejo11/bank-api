package io.github.alexistrejo11.bank.notifications.application.handler.query;

import io.github.alexistrejo11.bank.notifications.domain.model.NotificationLogRecord;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationRecordFilters;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationLogRepository;
import io.github.alexistrejo11.bank.shared.page.PageResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListNotificationRecordsHandler {

	private final NotificationLogRepository notificationLogRepository;

	public ListNotificationRecordsHandler(NotificationLogRepository notificationLogRepository) {
		this.notificationLogRepository = notificationLogRepository;
	}

	@Transactional(readOnly = true)
	public PageResult<NotificationLogRecord> handle(NotificationRecordFilters filters, int page, int size) {
		return notificationLogRepository.search(filters, page, size);
	}
}
