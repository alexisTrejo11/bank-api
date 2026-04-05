package io.github.alexistrejo11.bank.integration.loans;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import io.github.alexistrejo11.bank.loans.domain.port.out.CustomerCheckingAccountPort;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CustomerCheckingAccountAdapter implements CustomerCheckingAccountPort {

	private final AccountJpaRepository accountRepository;

	public CustomerCheckingAccountAdapter(AccountJpaRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public Optional<OwnedCheckingAccount> findOwnedChecking(UserId userId, UUID accountId) {
		return accountRepository.findByIdAndUserId(accountId, userId.value())
				.filter(a -> a.getType() == AccountType.CHECKING && a.getStatus() == AccountStatus.ACTIVE)
				.map(a -> new OwnedCheckingAccount(a.getId(), a.getCurrency()));
	}
}
