package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.LedgerEntryEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryEntity, UUID> {

	@Query(value = """
			SELECT COALESCE(SUM(CASE WHEN entry_type = 'CREDIT' THEN amount ELSE -amount END), 0)
			FROM ledger_entries WHERE account_id = :accountId
			""", nativeQuery = true)
	BigDecimal sumBalance(@Param("accountId") UUID accountId);

	Page<LedgerEntryEntity> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
}
