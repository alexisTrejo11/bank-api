package io.github.alexistrejo11.bank.loans.domain.port.out;

import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.util.Optional;
import java.util.UUID;

public interface CustomerCheckingAccountPort {

	Optional<OwnedCheckingAccount> findOwnedChecking(UserId userId, UUID accountId);

	record OwnedCheckingAccount(UUID id, String currencyCode) {
	}
}
