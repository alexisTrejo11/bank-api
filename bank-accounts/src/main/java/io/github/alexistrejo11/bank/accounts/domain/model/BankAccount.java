package io.github.alexistrejo11.bank.accounts.domain.model;

import java.time.Instant;
import java.util.UUID;

public record BankAccount(
		UUID id,
		UUID userId,
		AccountType type,
		String currency,
		AccountStatus status,
		Instant createdAt,
		Instant updatedAt,
		Instant deletedAt
) {
}
