package io.github.alexistrejo11.bank.accounts.domain.port.out;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountSummary;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

	void save(UUID id, UUID userId, AccountType type, String currency, AccountStatus status, Instant createdAt, Instant updatedAt);

	Optional<AccountSummary> findById(UUID id);

	Optional<AccountSummary> findByIdAndUserId(UUID id, UUID userId);

	Optional<OwnedCheckingAccount> findOwnedActiveChecking(UserId userId, UUID accountId);

	record OwnedCheckingAccount(UUID id, String currencyCode) {
	}
}
