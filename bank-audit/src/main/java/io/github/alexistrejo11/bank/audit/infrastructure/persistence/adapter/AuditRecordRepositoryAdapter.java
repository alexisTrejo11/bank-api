package io.github.alexistrejo11.bank.audit.infrastructure.persistence.adapter;

import io.github.alexistrejo11.bank.audit.domain.model.AuditRecord;
import io.github.alexistrejo11.bank.audit.domain.model.AuditRecordFilters;
import io.github.alexistrejo11.bank.audit.domain.port.out.AuditRecordRepository;
import io.github.alexistrejo11.bank.audit.infrastructure.persistence.entity.AuditRecordEntity;
import io.github.alexistrejo11.bank.audit.infrastructure.persistence.repository.AuditRecordJpaRepository;
import io.github.alexistrejo11.bank.audit.infrastructure.persistence.repository.AuditRecordSpecifications;
import io.github.alexistrejo11.bank.shared.page.PageResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class AuditRecordRepositoryAdapter implements AuditRecordRepository {

	private final AuditRecordJpaRepository jpaRepository;

	public AuditRecordRepositoryAdapter(AuditRecordJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void append(AuditRecord record) {
		jpaRepository.save(new AuditRecordEntity(
				record.id(),
				record.eventType(),
				record.actorId(),
				record.entityType(),
				record.entityId(),
				record.payload(),
				record.createdAt()
		));
	}

	@Override
	public PageResult<AuditRecord> search(AuditRecordFilters filters, int page, int size) {
		var springPage = jpaRepository.findAll(AuditRecordSpecifications.matching(filters), PageRequest.of(page, size));
		return new PageResult<>(
				springPage.getContent().stream().map(this::toDomain).toList(),
				springPage.getTotalElements(),
				springPage.getNumber(),
				springPage.getSize()
		);
	}

	private AuditRecord toDomain(AuditRecordEntity e) {
		return new AuditRecord(
				e.getId(),
				e.getEventType(),
				e.getActorId(),
				e.getEntityType(),
				e.getEntityId(),
				e.getPayload(),
				e.getCreatedAt()
		);
	}
}
