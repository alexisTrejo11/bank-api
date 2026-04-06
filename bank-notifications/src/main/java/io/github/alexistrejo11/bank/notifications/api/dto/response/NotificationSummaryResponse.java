package io.github.alexistrejo11.bank.notifications.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Aggregate notification counts.")
public record NotificationSummaryResponse(
		@Schema(description = "Total notifications in scope") long total,
		@Schema(description = "Counts grouped by status") List<NotificationStatusCountResponse> byStatus
) {
}
