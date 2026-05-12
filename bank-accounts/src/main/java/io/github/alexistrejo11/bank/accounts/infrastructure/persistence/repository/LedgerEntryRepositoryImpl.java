package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntry;
import io.github.alexistrejo11.bank.accounts.domain.repository.LedgerEntryRepository;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.LedgerEntryJpaEntity;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.jpa.LedgerEntryJpaRepository;
import io.github.alexistrejo11.bank.shared.shared_kernel.page.PageResult;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class LedgerEntryRepositoryImpl implements LedgerEntryRepository {

	private final LedgerEntryJpaRepository jpaRepository;

	public LedgerEntryRepositoryImpl(LedgerEntryJpaRepository jpaRepository) {
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
				springPage.getSize());
	}

	private LedgerEntryJpaEntity toEntity(LedgerEntry e) {
		return LedgerEntryJpaEntity.builder()
				.id(e.id())
				.accountId(e.accountId())
				.entryType(e.entryType())
				.amount(e.amount())
				.currency(e.currency())
				.referenceType(e.referenceType())
				.referenceId(e.referenceId())
				.createdAt(e.createdAt())
				.build();
	}

	private LedgerEntry toDomain(LedgerEntryJpaEntity e) {
		return new LedgerEntry(
				e.getId(),
				e.getAccountId(),
				e.getEntryType(),
				e.getAmount(),
				e.getCurrency(),
				e.getReferenceType(),
				e.getReferenceId(),
				e.getCreatedAt());
	}
}
