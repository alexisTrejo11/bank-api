package io.github.alexistrejo11.bank.payments.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.payments.infrastructure.persistence.entity.TransferJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferJpaRepository extends JpaRepository<TransferJpaEntity, UUID> {

	Optional<TransferJpaEntity> findByUserIdAndIdempotencyKey(UUID userId, UUID idempotencyKey);
}
