package io.github.alexistrejo11.bank.accounts.application;

import io.github.alexistrejo11.bank.accounts.api.dto.request.OpenAccountRequest;
import io.github.alexistrejo11.bank.accounts.api.dto.response.BalanceResponse;
import io.github.alexistrejo11.bank.accounts.api.dto.response.LedgerEntryResponse;
import io.github.alexistrejo11.bank.accounts.api.dto.response.LedgerPageResponse;
import io.github.alexistrejo11.bank.accounts.api.dto.response.OpenAccountResponse;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.exception.AccountNotFoundException;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.AccountEntity;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.LedgerEntryEntity;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountApplicationService {

	private final AccountJpaRepository accountRepository;
	private final LedgerEntryJpaRepository ledgerEntryRepository;

	public AccountApplicationService(AccountJpaRepository accountRepository, LedgerEntryJpaRepository ledgerEntryRepository) {
		this.accountRepository = accountRepository;
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	@Transactional
	public OpenAccountResponse open(UserId ownerId, OpenAccountRequest request) {
		UUID id = UUID.randomUUID();
		Instant now = Instant.now();
		String currency = request.currency().trim().toUpperCase();
		AccountEntity entity = new AccountEntity(
				id,
				ownerId.value(),
				request.type(),
				currency,
				AccountStatus.ACTIVE,
				now,
				now
		);
		accountRepository.save(entity);
		return new OpenAccountResponse(id, currency, request.type());
	}

	@Transactional(readOnly = true)
	public BalanceResponse getBalance(UserId ownerId, UUID accountId) {
		AccountEntity acc = accountRepository.findByIdAndUserId(accountId, ownerId.value())
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));
		BigDecimal balance = ledgerEntryRepository.sumBalance(accountId);
		if (balance == null) {
			balance = BigDecimal.ZERO;
		}
		return new BalanceResponse(balance, acc.getCurrency());
	}

	@Transactional(readOnly = true)
	public LedgerPageResponse getLedger(UserId ownerId, UUID accountId, Pageable pageable) {
		accountRepository.findByIdAndUserId(accountId, ownerId.value())
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));
		Page<LedgerEntryEntity> page = ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable);
		List<LedgerEntryResponse> content = page.getContent().stream().map(this::toLedgerEntryResponse).toList();
		return new LedgerPageResponse(
				content,
				page.getTotalElements(),
				page.getNumber(),
				page.getSize()
		);
	}

	private LedgerEntryResponse toLedgerEntryResponse(LedgerEntryEntity e) {
		return new LedgerEntryResponse(
				e.getId(),
				e.getEntryType(),
				e.getAmount(),
				e.getCurrency(),
				e.getReferenceType(),
				e.getReferenceId(),
				e.getCreatedAt()
		);
	}
}
