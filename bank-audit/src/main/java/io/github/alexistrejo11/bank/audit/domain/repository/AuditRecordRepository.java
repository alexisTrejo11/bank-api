package io.github.alexistrejo11.bank.audit.domain.repository;

import io.github.alexistrejo11.bank.audit.domain.model.AuditRecord;
import io.github.alexistrejo11.bank.audit.application.query.AuditRecordFilters;
import io.github.alexistrejo11.bank.shared.shared_kernel.page.PageResult;

public interface AuditRecordRepository {

	void append(AuditRecord record);

	PageResult<AuditRecord> search(AuditRecordFilters filters, int page, int size);
}
