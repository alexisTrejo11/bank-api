package io.github.alexistrejo11.bank.audit.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.audit.domain.model.AuditRecord;
import io.github.alexistrejo11.bank.audit.application.query.AuditRecordFilters;
import io.github.alexistrejo11.bank.audit.domain.repository.AuditRecordRepository;
import io.github.alexistrejo11.bank.audit.infrastructure.persistence.entity.AuditRecordEntity;
import io.github.alexistrejo11.bank.shared.shared_kernel.page.PageResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class AuditRecordRepositoryImpl implements AuditRecordRepository {

	private final AuditRecordJpaRepository jpaRepository;

	public AuditRecordRepositoryImpl(AuditRecordJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void append(AuditRecord record) {
		jpaRepository.save(AuditRecordEntity.builder()
				.id(record.id())
				.eventType(record.eventType())
				.actorId(record.actorId())
				.entityType(record.entityType())
				.entityId(record.entityId())
				.payload(record.payload())
				.createdAt(record.createdAt())
				.build());
	}

	@Override
	public PageResult<AuditRecord> search(AuditRecordFilters filters, int page, int size) {
		var springPage = jpaRepository.findAll(AuditRecordSpecifications.matching(filters), PageRequest.of(page, size));
		return new PageResult<>(
				springPage.getContent().stream().map(this::toDomain).toList(),
				springPage.getTotalElements(),
				springPage.getNumber(),
				springPage.getSize());
	}

	private AuditRecord toDomain(AuditRecordEntity e) {
		return new AuditRecord(
				e.getId(),
				e.getEventType(),
				e.getActorId(),
				e.getEntityType(),
				e.getEntityId(),
				e.getPayload(),
				e.getCreatedAt());
	}
}
