package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.adapter;

import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntry;
import io.github.alexistrejo11.bank.accounts.domain.port.out.LedgerEntryRepository;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.LedgerEntryEntity;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import io.github.alexistrejo11.bank.shared.page.PageResult;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class LedgerEntryRepositoryAdapter implements LedgerEntryRepository {

	private final LedgerEntryJpaRepository jpaRepository;

	public LedgerEntryRepositoryAdapter(LedgerEntryJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public BigDecimal sumBalance(UUID accountId) {
		BigDecimal sum = jpaRepository.sumBalance(accountId);
		return sum != null ? sum : BigDecimal.ZERO;
	}

	@Override
	public void savePair(LedgerEntry debit, LedgerEntry credit) {
		jpaRepository.save(toEntity(debit));
		jpaRepository.save(toEntity(credit));
	}

	@Override
	public PageResult<LedgerEntry> findPageByAccountId(UUID accountId, int page, int size) {
		var springPage = jpaRepository.findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(page, size));
		return new PageResult<>(
				springPage.getContent().stream().map(this::toDomain).toList(),
				springPage.getTotalElements(),
				springPage.getNumber(),
				springPage.getSize()
		);
	}

	private LedgerEntryEntity toEntity(LedgerEntry e) {
		return new LedgerEntryEntity(
				e.id(),
				e.accountId(),
				e.entryType(),
				e.amount(),
				e.currency(),
				e.referenceType(),
				e.referenceId(),
				e.createdAt()
		);
	}

	private LedgerEntry toDomain(LedgerEntryEntity e) {
		return new LedgerEntry(
				e.getId(),
				e.getAccountId(),
				e.getEntryType(),
				e.getAmount(),
				e.getCurrency(),
				e.getReferenceType(),
				e.getReferenceId(),
				e.getCreatedAt()
		);
	}
}
