package io.github.alexistrejo11.bank.audit.application.handler.query;

import io.github.alexistrejo11.bank.audit.application.query.AuditRecordFilters;
import io.github.alexistrejo11.bank.audit.domain.model.AuditRecord;
import io.github.alexistrejo11.bank.audit.domain.repository.AuditRecordRepository;
import io.github.alexistrejo11.bank.shared.shared_kernel.page.PageResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SearchAuditRecordsHandler {

	private final AuditRecordRepository auditRecordRepository;

	public SearchAuditRecordsHandler(AuditRecordRepository auditRecordRepository) {
		this.auditRecordRepository = auditRecordRepository;
	}

	@Transactional(readOnly = true)
	public PageResult<AuditRecord> handle(AuditRecordFilters filters, int page, int size) {
		return auditRecordRepository.search(filters, page, size);
	}
}
