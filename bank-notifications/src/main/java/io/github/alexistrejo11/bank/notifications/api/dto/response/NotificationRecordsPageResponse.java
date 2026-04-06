package io.github.alexistrejo11.bank.notifications.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated notification log.")
public record NotificationRecordsPageResponse(
		@Schema(description = "Page content") List<NotificationRecordResponse> content,
		@Schema(description = "Zero-based page") int page,
		@Schema(description = "Page size") int size,
		@Schema(description = "Total rows") long totalElements
) {
}
