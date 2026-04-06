package io.github.alexistrejo11.bank.notifications.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Count for one notification status bucket.")
public record NotificationStatusCountResponse(
		@Schema(description = "Status value") String status,
		@Schema(description = "Number of rows") long count
) {
}
