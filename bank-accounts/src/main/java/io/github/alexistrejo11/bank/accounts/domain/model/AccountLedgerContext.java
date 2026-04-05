package io.github.alexistrejo11.bank.accounts.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountLedgerContext(
		UUID accountId,
		UUID userId,
		String currencyCode,
		BigDecimal ledgerBalance,
		AccountStatus status
) {

	public boolean active() {
		return status == AccountStatus.ACTIVE;
	}
}
