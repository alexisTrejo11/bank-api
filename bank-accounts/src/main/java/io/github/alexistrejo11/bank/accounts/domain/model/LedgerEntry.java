package io.github.alexistrejo11.bank.accounts.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntry(
		UUID id,
		UUID accountId,
		LedgerEntryType entryType,
		BigDecimal amount,
		String currency,
		String referenceType,
		UUID referenceId,
		Instant createdAt
) {
}
