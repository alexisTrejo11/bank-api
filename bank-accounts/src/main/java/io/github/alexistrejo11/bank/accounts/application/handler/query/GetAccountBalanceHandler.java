package io.github.alexistrejo11.bank.accounts.application.handler.query;

import io.github.alexistrejo11.bank.accounts.domain.exception.AccountNotFoundException;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountBalance;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountSummary;
import io.github.alexistrejo11.bank.accounts.domain.port.out.AccountRepository;
import io.github.alexistrejo11.bank.accounts.domain.port.out.LedgerEntryRepository;
import io.github.alexistrejo11.bank.accounts.domain.query.GetAccountBalanceQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetAccountBalanceHandler {

	private final AccountRepository accountRepository;
	private final LedgerEntryRepository ledgerEntryRepository;

	public GetAccountBalanceHandler(AccountRepository accountRepository, LedgerEntryRepository ledgerEntryRepository) {
		this.accountRepository = accountRepository;
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	@Transactional(readOnly = true)
	public AccountBalance handle(GetAccountBalanceQuery query) {
		AccountSummary acc = accountRepository.findByIdAndUserId(query.accountId(), query.ownerId().value())
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));
		return new AccountBalance(ledgerEntryRepository.sumBalance(query.accountId()), acc.currency());
	}
}
