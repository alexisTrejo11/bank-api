package io.github.alexistrejo11.bank.notifications.domain.port.out;

import io.github.alexistrejo11.bank.notifications.domain.model.NotificationLogRecord;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationRecordFilters;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationSummaryModel;
import io.github.alexistrejo11.bank.shared.page.PageResult;

public interface NotificationLogRepository {

	NotificationLogRecord save(NotificationLogRecord record);

	PageResult<NotificationLogRecord> search(NotificationRecordFilters filters, int page, int size);

	NotificationSummaryModel summaryByStatus();
}
