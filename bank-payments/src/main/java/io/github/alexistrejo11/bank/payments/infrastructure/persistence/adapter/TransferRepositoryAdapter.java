package io.github.alexistrejo11.bank.payments.infrastructure.persistence.adapter;

import io.github.alexistrejo11.bank.payments.domain.model.TransferRecord;
import io.github.alexistrejo11.bank.payments.domain.model.TransferStatus;
import io.github.alexistrejo11.bank.payments.domain.port.out.TransferRepository;
import io.github.alexistrejo11.bank.payments.infrastructure.persistence.entity.TransferEntity;
import io.github.alexistrejo11.bank.payments.infrastructure.persistence.repository.TransferJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TransferRepositoryAdapter implements TransferRepository {

	private final TransferJpaRepository jpaRepository;

	public TransferRepositoryAdapter(TransferJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Optional<TransferRecord> findByUserIdAndIdempotencyKey(UUID userId, UUID idempotencyKey) {
		return jpaRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey).map(this::toRecord);
	}

	@Override
	public Optional<TransferRecord> findById(UUID id) {
		return jpaRepository.findById(id).map(this::toRecord);
	}

	@Override
	public TransferRecord save(TransferRecord transfer) {
		TransferEntity entity = toEntity(transfer);
		return toRecord(jpaRepository.save(entity));
	}

	private TransferEntity toEntity(TransferRecord r) {
		return new TransferEntity(
				r.id(),
				r.userId(),
				r.sourceAccountId(),
				r.targetAccountId(),
				r.amount(),
				r.currency(),
				r.status(),
				r.idempotencyKey(),
				r.failureReason(),
				r.referenceTransferId(),
				r.createdAt(),
				r.updatedAt()
		);
	}

	private TransferRecord toRecord(TransferEntity e) {
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
				e.getUpdatedAt()
		);
	}
}
