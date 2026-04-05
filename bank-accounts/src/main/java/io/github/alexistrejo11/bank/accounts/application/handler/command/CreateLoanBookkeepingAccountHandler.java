package io.github.alexistrejo11.bank.accounts.application.handler.command;

import io.github.alexistrejo11.bank.accounts.domain.command.CreateLoanBookkeepingAccountCommand;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.github.alexistrejo11.bank.accounts.domain.port.in.command.CreateLoanBookkeepingAccountUseCase;
import io.github.alexistrejo11.bank.accounts.domain.port.out.AccountRepository;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
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
		accountRepository.save(id, command.borrowerId().value(), AccountType.LOAN, ccy, AccountStatus.ACTIVE, now, now);
		return AccountId.of(id);
	}
}
