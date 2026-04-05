package io.github.alexistrejo11.bank.accounts.domain.model;

import java.util.UUID;

public record AccountSummary(
		UUID id,
		UUID userId,
		AccountType type,
		String currency,
		AccountStatus status
) {
}
