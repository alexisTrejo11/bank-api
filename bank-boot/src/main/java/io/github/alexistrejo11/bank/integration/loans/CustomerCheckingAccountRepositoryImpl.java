package io.github.alexistrejo11.bank.integration.loans;

import io.github.alexistrejo11.bank.accounts.domain.repository.AccountRepository;
import io.github.alexistrejo11.bank.loans.domain.repository.CustomerCheckingAccountRepository;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CustomerCheckingAccountRepositoryImpl implements CustomerCheckingAccountRepository {

	private final AccountRepository accountRepository;

	public CustomerCheckingAccountRepositoryImpl(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public Optional<OwnedCheckingAccount> findOwnedChecking(UserId userId, UUID accountId) {
		return accountRepository.findOwnedActiveChecking(userId, accountId)
				.map(o -> new OwnedCheckingAccount(o.id(), o.currencyCode()));
	}
}
