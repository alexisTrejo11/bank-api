package io.github.alexistrejo11.bank.loans.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.loans.infrastructure.persistence.entity.LoanEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanJpaRepository extends JpaRepository<LoanEntity, UUID> {

	@Query("""
			select distinct l from LoanEntity l
			left join fetch l.repayments r
			where l.id = :id and l.userId = :userId
			""")
	Optional<LoanEntity> findByIdAndUserIdWithRepayments(@Param("id") UUID id, @Param("userId") UUID userId);

	Optional<LoanEntity> findByIdAndUserId(UUID id, UUID userId);
}
