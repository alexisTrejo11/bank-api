package io.github.alexistrejo11.bank.loans.domain.repository;

import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.util.Optional;
import java.util.UUID;

public interface CustomerCheckingAccountRepository {

	Optional<OwnedCheckingAccount> findOwnedChecking(UserId userId, UUID accountId);

	record OwnedCheckingAccount(UUID id, String currencyCode) {
	}
}
