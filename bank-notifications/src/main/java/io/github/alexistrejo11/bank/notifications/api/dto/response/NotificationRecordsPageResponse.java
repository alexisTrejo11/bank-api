package io.github.alexistrejo11.bank.notifications.api.dto.response;

import java.util.List;

public record NotificationRecordsPageResponse(List<NotificationRecordResponse> content, int page, int size, long totalElements) {
}
