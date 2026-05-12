package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.jpa;

import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.AccountJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

	Optional<AccountJpaEntity> findByIdAndUserId(UUID id, UUID userId);
}
