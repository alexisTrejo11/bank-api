package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountLedgerContext;
import io.github.alexistrejo11.bank.accounts.domain.repository.AccountLedgerQueryPort;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.jpa.AccountJpaRepository;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.jpa.LedgerEntryJpaRepository;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.AccountId;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AccountLedgerQueryRepositoryImpl implements AccountLedgerQueryPort {

	private final AccountJpaRepository accountJpaRepository;
	private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

	public AccountLedgerQueryRepositoryImpl(AccountJpaRepository accountJpaRepository,
			LedgerEntryJpaRepository ledgerEntryJpaRepository) {
		this.accountJpaRepository = accountJpaRepository;
		this.ledgerEntryJpaRepository = ledgerEntryJpaRepository;
	}

	@Override
	public Optional<AccountLedgerContext> findByAccountId(AccountId accountId) {
		UUID id = accountId.value();
		return accountJpaRepository.findById(id).map(ent -> {
			BigDecimal bal = ledgerEntryJpaRepository.sumBalance(id);
			if (bal == null) {
				bal = BigDecimal.ZERO;
			}
			return new AccountLedgerContext(id, ent.getUserId(), ent.getCurrency(), bal, ent.getStatus());
		});
	}
}
