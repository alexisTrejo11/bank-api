package io.github.alexistrejo11.bank.notifications.api.dto.response;

import java.util.List;

public record NotificationSummaryResponse(long total, List<NotificationStatusCountResponse> byStatus) {
}
