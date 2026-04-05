package io.github.alexistrejo11.bank.audit.api.mapper;

import io.github.alexistrejo11.bank.audit.api.dto.response.AuditRecordResponse;
import io.github.alexistrejo11.bank.audit.api.dto.response.AuditRecordsPageResponse;
import io.github.alexistrejo11.bank.audit.domain.model.AuditRecord;
import io.github.alexistrejo11.bank.shared.page.PageResult;
import java.util.List;

public final class AuditApiMapper {

	private AuditApiMapper() {
	}

	public static AuditRecordsPageResponse toPageResponse(PageResult<AuditRecord> page) {
		List<AuditRecordResponse> content = page.content().stream().map(AuditApiMapper::toResponse).toList();
		return new AuditRecordsPageResponse(content, page.totalElements(), page.page(), page.size());
	}

	private static AuditRecordResponse toResponse(AuditRecord r) {
		return new AuditRecordResponse(
				r.id(),
				r.eventType(),
				r.actorId(),
				r.entityType(),
				r.entityId(),
				r.payload(),
				r.createdAt()
		);
	}
}
