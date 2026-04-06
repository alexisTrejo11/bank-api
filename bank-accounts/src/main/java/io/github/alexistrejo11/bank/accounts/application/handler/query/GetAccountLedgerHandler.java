package io.github.alexistrejo11.bank.accounts.application.handler.query;

import io.github.alexistrejo11.bank.accounts.domain.exception.AccountNotFoundException;
import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntry;
import io.github.alexistrejo11.bank.accounts.domain.port.out.AccountRepository;
import io.github.alexistrejo11.bank.accounts.domain.port.out.LedgerEntryRepository;
import io.github.alexistrejo11.bank.accounts.domain.query.GetAccountLedgerQuery;
import io.github.alexistrejo11.bank.shared.page.PageResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetAccountLedgerHandler {

	private final AccountRepository accountRepository;
	private final LedgerEntryRepository ledgerEntryRepository;

	public GetAccountLedgerHandler(AccountRepository accountRepository, LedgerEntryRepository ledgerEntryRepository) {
		this.accountRepository = accountRepository;
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	@Transactional(readOnly = true)
	public PageResult<LedgerEntry> handle(GetAccountLedgerQuery query) {
		accountRepository.findByIdAndUserId(query.accountId(), query.ownerId().value())
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));
		return ledgerEntryRepository.findPageByAccountId(query.accountId(), query.page(), query.size());
	}
}
