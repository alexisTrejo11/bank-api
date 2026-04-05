package io.github.alexistrejo11.bank.payments.api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
		UUID id,
		String status,
		UUID sourceAccountId,
		UUID targetAccountId,
		BigDecimal amount,
		String currency,
		UUID referenceTransferId,
		String failureReason,
		Instant createdAt
) {
}
