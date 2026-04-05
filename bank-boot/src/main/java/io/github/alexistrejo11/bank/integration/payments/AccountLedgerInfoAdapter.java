package io.github.alexistrejo11.bank.integration.payments;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import io.github.alexistrejo11.bank.payments.domain.port.out.AccountLedgerInfoPort;
import io.github.alexistrejo11.bank.payments.domain.port.out.AccountLedgerInfoPort.AccountLedgerInfo;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AccountLedgerInfoAdapter implements AccountLedgerInfoPort {

	private final AccountJpaRepository accountRepository;
	private final LedgerEntryJpaRepository ledgerEntryRepository;

	public AccountLedgerInfoAdapter(AccountJpaRepository accountRepository, LedgerEntryJpaRepository ledgerEntryRepository) {
		this.accountRepository = accountRepository;
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	@Override
	public Optional<AccountLedgerInfo> find(AccountId accountId) {
		UUID id = accountId.value();
		return accountRepository.findById(id).map(acc -> {
			BigDecimal bal = ledgerEntryRepository.sumBalance(id);
			if (bal == null) {
				bal = BigDecimal.ZERO;
			}
			return new AccountLedgerInfo(
					id,
					new UserId(acc.getUserId()),
					acc.getCurrency(),
					bal,
					acc.getStatus() == AccountStatus.ACTIVE
			);
		});
	}
}
