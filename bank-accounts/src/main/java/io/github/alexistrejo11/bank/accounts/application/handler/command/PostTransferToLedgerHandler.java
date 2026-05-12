package io.github.alexistrejo11.bank.accounts.application.handler.command;

import io.github.alexistrejo11.bank.accounts.application.command.PostTransferToLedgerCommand;
import io.github.alexistrejo11.bank.accounts.domain.exception.AccountNotFoundException;
import io.github.alexistrejo11.bank.accounts.domain.exception.InvalidTransferException;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.BankAccount;
import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntry;
import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntryType;
import io.github.alexistrejo11.bank.accounts.application.PostTransferToLedgerUseCase;
import io.github.alexistrejo11.bank.accounts.domain.repository.AccountRepository;
import io.github.alexistrejo11.bank.accounts.domain.repository.LedgerEntryRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PostTransferToLedgerHandler implements PostTransferToLedgerUseCase {

	private final AccountRepository accountRepository;
	private final LedgerEntryRepository ledgerEntryRepository;

	public PostTransferToLedgerHandler(AccountRepository accountRepository, LedgerEntryRepository ledgerEntryRepository) {
		this.accountRepository = accountRepository;
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	@Override
	@Transactional
	public void execute(PostTransferToLedgerCommand command) {
		BankAccount fromAcc = accountRepository.findById(command.from().value())
				.orElseThrow(() -> new AccountNotFoundException("Source account not found"));
		BankAccount toAcc = accountRepository.findById(command.to().value())
				.orElseThrow(() -> new AccountNotFoundException("Target account not found"));

		if (fromAcc.status() != AccountStatus.ACTIVE || toAcc.status() != AccountStatus.ACTIVE) {
			throw new InvalidTransferException("Account is not active");
		}

		String ccy = command.currencyCode() != null ? command.currencyCode().toUpperCase() : "";
		if (!fromAcc.currency().equals(ccy) || !toAcc.currency().equals(ccy)) {
			throw new InvalidTransferException("Currency mismatch between accounts and transfer");
		}

		Instant now = Instant.now();
		UUID ref = command.referenceId() != null ? command.referenceId() : UUID.randomUUID();

		LedgerEntry debit = new LedgerEntry(
				UUID.randomUUID(),
				command.from().value(),
				LedgerEntryType.DEBIT,
				command.amount(),
				ccy,
				command.referenceType(),
				ref,
				now);

		LedgerEntry credit = new LedgerEntry(
				UUID.randomUUID(),
				command.to().value(),
				LedgerEntryType.CREDIT,
				command.amount(),
				ccy,
				command.referenceType(),
				ref,
				now);

		ledgerEntryRepository.savePair(debit, credit);
	}
}
