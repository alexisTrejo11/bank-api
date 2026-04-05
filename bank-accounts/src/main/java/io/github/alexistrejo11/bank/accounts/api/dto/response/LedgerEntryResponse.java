package io.github.alexistrejo11.bank.accounts.api.dto.response;

import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntryType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponse(
		UUID id,
		LedgerEntryType entryType,
		BigDecimal amount,
		String currency,
		String referenceType,
		UUID referenceId,
		Instant createdAt
) {
}
