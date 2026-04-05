package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.AccountEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

	Optional<AccountEntity> findByIdAndUserId(UUID id, UUID userId);
}
