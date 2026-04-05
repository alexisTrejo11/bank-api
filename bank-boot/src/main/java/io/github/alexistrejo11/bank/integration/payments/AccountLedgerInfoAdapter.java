package io.github.alexistrejo11.bank.integration.payments;

import io.github.alexistrejo11.bank.accounts.domain.port.out.AccountLedgerQueryPort;
import io.github.alexistrejo11.bank.payments.domain.port.out.AccountLedgerInfoPort;
import io.github.alexistrejo11.bank.payments.domain.port.out.AccountLedgerInfoPort.AccountLedgerInfo;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AccountLedgerInfoAdapter implements AccountLedgerInfoPort {

	private final AccountLedgerQueryPort accountLedgerQueryPort;

	public AccountLedgerInfoAdapter(AccountLedgerQueryPort accountLedgerQueryPort) {
		this.accountLedgerQueryPort = accountLedgerQueryPort;
	}

	@Override
	public Optional<AccountLedgerInfo> find(AccountId accountId) {
		return accountLedgerQueryPort.findByAccountId(accountId).map(ctx -> new AccountLedgerInfo(
				ctx.accountId(),
				new UserId(ctx.userId()),
				ctx.currencyCode(),
				ctx.ledgerBalance(),
				ctx.active()
		));
	}
}
