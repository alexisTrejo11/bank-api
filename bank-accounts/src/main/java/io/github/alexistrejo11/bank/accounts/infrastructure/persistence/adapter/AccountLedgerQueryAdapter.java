package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.adapter;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountLedgerContext;
import io.github.alexistrejo11.bank.accounts.domain.port.out.AccountLedgerQueryPort;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AccountLedgerQueryAdapter implements AccountLedgerQueryPort {

	private final AccountJpaRepository accountJpaRepository;
	private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

	public AccountLedgerQueryAdapter(AccountJpaRepository accountJpaRepository, LedgerEntryJpaRepository ledgerEntryJpaRepository) {
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
