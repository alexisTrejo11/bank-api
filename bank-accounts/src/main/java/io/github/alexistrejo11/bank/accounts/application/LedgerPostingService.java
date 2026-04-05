package io.github.alexistrejo11.bank.accounts.application;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntryType;
import io.github.alexistrejo11.bank.accounts.exception.AccountNotFoundException;
import io.github.alexistrejo11.bank.accounts.exception.InvalidTransferException;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.AccountEntity;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.LedgerEntryEntity;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerPostingService {

	private final AccountJpaRepository accountRepository;
	private final LedgerEntryJpaRepository ledgerEntryRepository;

	public LedgerPostingService(AccountJpaRepository accountRepository, LedgerEntryJpaRepository ledgerEntryRepository) {
		this.accountRepository = accountRepository;
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	/**
	 * Double-entry: debit source account, credit target account for the same reference.
	 */
	@Transactional
	public void postTransfer(
			AccountId from,
			AccountId to,
			BigDecimal amount,
			String currencyCode,
			String referenceType,
			UUID referenceId
	) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidTransferException("Amount must be positive");
		}
		AccountEntity fromAcc = accountRepository.findById(from.value())
				.orElseThrow(() -> new AccountNotFoundException("Source account not found"));
		AccountEntity toAcc = accountRepository.findById(to.value())
				.orElseThrow(() -> new AccountNotFoundException("Target account not found"));
		if (fromAcc.getStatus() != AccountStatus.ACTIVE || toAcc.getStatus() != AccountStatus.ACTIVE) {
			throw new InvalidTransferException("Account is not active");
		}
		String ccy = currencyCode != null ? currencyCode.toUpperCase() : "";
		if (!fromAcc.getCurrency().equals(ccy) || !toAcc.getCurrency().equals(ccy)) {
			throw new InvalidTransferException("Currency mismatch between accounts and transfer");
		}
		Instant now = Instant.now();
		UUID ref = referenceId != null ? referenceId : UUID.randomUUID();
		LedgerEntryEntity debit = new LedgerEntryEntity(
				UUID.randomUUID(),
				from.value(),
				LedgerEntryType.DEBIT,
				amount,
				ccy,
				referenceType,
				ref,
				now
		);
		LedgerEntryEntity credit = new LedgerEntryEntity(
				UUID.randomUUID(),
				to.value(),
				LedgerEntryType.CREDIT,
				amount,
				ccy,
				referenceType,
				ref,
				now
		);
		ledgerEntryRepository.save(debit);
		ledgerEntryRepository.save(credit);
	}
}
