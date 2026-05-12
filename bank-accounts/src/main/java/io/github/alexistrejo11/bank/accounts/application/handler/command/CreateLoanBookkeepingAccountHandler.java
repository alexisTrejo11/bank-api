package io.github.alexistrejo11.bank.accounts.application.handler.command;

import io.github.alexistrejo11.bank.accounts.application.command.CreateLoanBookkeepingAccountCommand;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.github.alexistrejo11.bank.accounts.domain.model.BankAccount;
import io.github.alexistrejo11.bank.accounts.application.CreateLoanBookkeepingAccountUseCase;
import io.github.alexistrejo11.bank.accounts.domain.repository.AccountRepository;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.AccountId;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateLoanBookkeepingAccountHandler implements CreateLoanBookkeepingAccountUseCase {

	private final AccountRepository accountRepository;

	public CreateLoanBookkeepingAccountHandler(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	@Transactional
	public AccountId execute(CreateLoanBookkeepingAccountCommand command) {
		Instant now = Instant.now();
		UUID id = UUID.randomUUID();
		String ccy = command.currencyCode().toUpperCase();
		BankAccount account = new BankAccount(id, command.borrowerId().value(), AccountType.LOAN, ccy, AccountStatus.ACTIVE,
				now, now, null);

		accountRepository.save(account);

		return AccountId.of(id);
	}
}
