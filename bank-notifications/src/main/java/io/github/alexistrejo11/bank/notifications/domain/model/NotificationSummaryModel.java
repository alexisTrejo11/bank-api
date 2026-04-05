package io.github.alexistrejo11.bank.notifications.domain.model;

import java.util.List;

public record NotificationSummaryModel(long total, List<NotificationStatusCount> byStatus) {

	public record NotificationStatusCount(NotificationStatus status, long count) {
	}
}
