package io.github.alexistrejo11.bank.payments.domain.port.out;

import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AccountLedgerInfoPort {

	Optional<AccountLedgerInfo> find(AccountId accountId);

	record AccountLedgerInfo(
			UUID accountId,
			UserId ownerId,
			String currencyCode,
			BigDecimal ledgerBalance,
			boolean active
	) {
	}
}
