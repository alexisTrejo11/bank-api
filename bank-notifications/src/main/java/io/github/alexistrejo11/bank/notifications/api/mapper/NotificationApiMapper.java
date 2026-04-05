package io.github.alexistrejo11.bank.notifications.api.mapper;

import io.github.alexistrejo11.bank.notifications.api.dto.response.NotificationRecordResponse;
import io.github.alexistrejo11.bank.notifications.api.dto.response.NotificationRecordsPageResponse;
import io.github.alexistrejo11.bank.notifications.api.dto.response.NotificationStatusCountResponse;
import io.github.alexistrejo11.bank.notifications.api.dto.response.NotificationSummaryResponse;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationLogRecord;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationSummaryModel;
import io.github.alexistrejo11.bank.shared.page.PageResult;
import java.util.List;

public final class NotificationApiMapper {

	private NotificationApiMapper() {
	}

	public static NotificationRecordsPageResponse toPageResponse(PageResult<NotificationLogRecord> page) {
		List<NotificationRecordResponse> content = page.content().stream().map(NotificationApiMapper::toRecordResponse).toList();
		return new NotificationRecordsPageResponse(content, page.page(), page.size(), page.totalElements());
	}

	public static NotificationSummaryResponse toSummaryResponse(NotificationSummaryModel model) {
		List<NotificationStatusCountResponse> byStatus = model.byStatus().stream()
				.map(e -> new NotificationStatusCountResponse(e.status().name(), e.count()))
				.toList();
		return new NotificationSummaryResponse(model.total(), byStatus);
	}

	private static NotificationRecordResponse toRecordResponse(NotificationLogRecord e) {
		return new NotificationRecordResponse(
				e.id(),
				e.userId(),
				e.channel().name(),
				e.templateKey(),
				e.status().name(),
				e.sourceEventType(),
				e.subject(),
				e.recipientHint(),
				e.createdAt(),
				e.dispatchedAt(),
				e.errorMessage()
		);
	}
}
