package io.github.alexistrejo11.bank.payments.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Transfer aggregate after initiation or reversal.")
public record TransferResponse(
		@Schema(description = "Transfer id") UUID id,
		@Schema(description = "PENDING, PROCESSING, COMPLETED, FAILED, REVERSED", example = "COMPLETED") String status,
		@Schema(description = "Source account") UUID sourceAccountId,
		@Schema(description = "Target account") UUID targetAccountId,
		@Schema(description = "Transfer amount") BigDecimal amount,
		@Schema(example = "USD") String currency,
		@Schema(description = "Set when this row is a reversal of another transfer") UUID referenceTransferId,
		@Schema(description = "Populated when status is FAILED") String failureReason,
		@Schema(description = "Creation time (UTC)") Instant createdAt
) {
}
