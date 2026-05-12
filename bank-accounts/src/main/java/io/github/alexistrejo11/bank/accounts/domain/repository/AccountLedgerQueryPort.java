package io.github.alexistrejo11.bank.accounts.domain.repository;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountLedgerContext;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.AccountId;
import java.util.Optional;

public interface AccountLedgerQueryPort {

	Optional<AccountLedgerContext> findByAccountId(AccountId accountId);
}
