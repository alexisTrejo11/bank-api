package io.github.alexistrejo11.bank.payments.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.payments.domain.model.TransferRecord;
import io.github.alexistrejo11.bank.payments.domain.repository.TransferRepository;
import io.github.alexistrejo11.bank.payments.infrastructure.persistence.entity.TransferJpaEntity;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TransferRepositoryImpl implements TransferRepository {

	private final TransferJpaRepository jpaRepository;

	public TransferRepositoryImpl(TransferJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Optional<TransferRecord> findByUserIdAndIdempotencyKey(UUID userId, UUID idempotencyKey) {
		return jpaRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
				.map(this::toRecord);
	}

	@Override
	public Optional<TransferRecord> findById(UUID id) {
		return jpaRepository.findById(id)
				.map(this::toRecord);
	}

	@Override
	public TransferRecord save(TransferRecord transfer) {
		TransferJpaEntity entity = toEntity(transfer);
		return toRecord(jpaRepository.save(entity));
	}

	private TransferJpaEntity toEntity(TransferRecord r) {
		return TransferJpaEntity.builder()
				.id(r.id())
				.userId(r.userId())
				.sourceAccountId(r.sourceAccountId())
				.targetAccountId(r.targetAccountId())
				.amount(r.amount())
				.currency(r.currency())
				.status(r.status())
				.idempotencyKey(r.idempotencyKey())
				.failureReason(r.failureReason())
				.referenceTransferId(r.referenceTransferId())
				.createdAt(r.createdAt())
				.updatedAt(r.updatedAt())
				.build();
	}

	private TransferRecord toRecord(TransferJpaEntity e) {
		return new TransferRecord(
				e.getId(),
				e.getUserId(),
				e.getSourceAccountId(),
				e.getTargetAccountId(),
				e.getAmount(),
				e.getCurrency(),
				e.getStatus(),
				e.getIdempotencyKey(),
				e.getFailureReason(),
				e.getReferenceTransferId(),
				e.getCreatedAt(),
				e.getUpdatedAt());
	}
}
