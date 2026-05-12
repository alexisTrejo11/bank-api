package io.github.alexistrejo11.bank.accounts.application.handler.command;

import io.github.alexistrejo11.bank.accounts.application.command.OpenAccountCommand;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.BankAccount;
import io.github.alexistrejo11.bank.accounts.domain.model.OpenedAccount;
import io.github.alexistrejo11.bank.accounts.domain.repository.AccountRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OpenAccountHandler {

	private final AccountRepository accountRepository;

	public OpenAccountHandler(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Transactional
	public OpenedAccount handle(OpenAccountCommand command) {
		UUID id = UUID.randomUUID();
		Instant now = Instant.now();
		String currency = command.currency().trim().toUpperCase();

		BankAccount account = new BankAccount(id, command.ownerId().value(), command.type(), currency, AccountStatus.ACTIVE,
				now, now, null);
		accountRepository.save(account);

		return new OpenedAccount(id, currency, command.type());
	}
}
