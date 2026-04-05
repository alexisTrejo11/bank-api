package io.github.alexistrejo11.bank.audit.application;

import io.github.alexistrejo11.bank.audit.api.dto.response.AuditRecordResponse;
import io.github.alexistrejo11.bank.audit.api.dto.response.AuditRecordsPageResponse;
import io.github.alexistrejo11.bank.audit.infrastructure.persistence.entity.AuditRecordEntity;
import io.github.alexistrejo11.bank.audit.infrastructure.persistence.repository.AuditRecordJpaRepository;
import io.github.alexistrejo11.bank.audit.infrastructure.persistence.repository.AuditRecordSpecifications;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditQueryService {

	private final AuditRecordJpaRepository auditRecordRepository;

	public AuditQueryService(AuditRecordJpaRepository auditRecordRepository) {
		this.auditRecordRepository = auditRecordRepository;
	}

	@Transactional(readOnly = true)
	public AuditRecordsPageResponse search(AuditRecordQuery query, Pageable pageable) {
		Page<AuditRecordEntity> page = auditRecordRepository.findAll(AuditRecordSpecifications.matching(query), pageable);
		List<AuditRecordResponse> content = page.getContent().stream().map(this::toResponse).toList();
		return new AuditRecordsPageResponse(content, page.getTotalElements(), page.getNumber(), page.getSize());
	}

	private AuditRecordResponse toResponse(AuditRecordEntity e) {
		return new AuditRecordResponse(
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
