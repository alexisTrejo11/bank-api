package io.github.alexistrejo11.bank.accounts.domain.port.out;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountLedgerContext;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import java.util.Optional;

public interface AccountLedgerQueryPort {

	Optional<AccountLedgerContext> findByAccountId(AccountId accountId);
}
