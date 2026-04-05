package io.github.alexistrejo11.bank.audit.domain.port.out;

import io.github.alexistrejo11.bank.audit.domain.model.AuditRecord;
import io.github.alexistrejo11.bank.audit.domain.model.AuditRecordFilters;
import io.github.alexistrejo11.bank.shared.page.PageResult;

public interface AuditRecordRepository {

	void append(AuditRecord record);

	PageResult<AuditRecord> search(AuditRecordFilters filters, int page, int size);
}
