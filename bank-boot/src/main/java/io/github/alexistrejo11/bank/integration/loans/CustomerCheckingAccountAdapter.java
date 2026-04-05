package io.github.alexistrejo11.bank.integration.loans;

import io.github.alexistrejo11.bank.accounts.domain.port.out.AccountRepository;
import io.github.alexistrejo11.bank.loans.domain.port.out.CustomerCheckingAccountPort;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CustomerCheckingAccountAdapter implements CustomerCheckingAccountPort {

	private final AccountRepository accountRepository;

	public CustomerCheckingAccountAdapter(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public Optional<OwnedCheckingAccount> findOwnedChecking(UserId userId, UUID accountId) {
		return accountRepository.findOwnedActiveChecking(userId, accountId)
				.map(o -> new OwnedCheckingAccount(o.id(), o.currencyCode()));
	}
}
