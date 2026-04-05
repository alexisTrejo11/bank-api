package io.github.alexistrejo11.bank.payments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferRecord(
		UUID id,
		UUID userId,
		UUID sourceAccountId,
		UUID targetAccountId,
		BigDecimal amount,
		String currency,
		TransferStatus status,
		UUID idempotencyKey,
		String failureReason,
		UUID referenceTransferId,
		Instant createdAt,
		Instant updatedAt
) {
}
