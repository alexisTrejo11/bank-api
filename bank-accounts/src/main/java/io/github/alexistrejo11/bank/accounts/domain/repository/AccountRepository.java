package io.github.alexistrejo11.bank.accounts.domain.repository;

import io.github.alexistrejo11.bank.accounts.domain.model.BankAccount;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

	void save(BankAccount account);

	Optional<BankAccount> findById(UUID id);

	Optional<BankAccount> findByIdAndUserId(UUID id, UUID userId);

	Optional<OwnedCheckingAccount> findOwnedActiveChecking(UserId userId, UUID accountId);

	record OwnedCheckingAccount(UUID id, String currencyCode) {
	}
}
