package io.github.alexistrejo11.bank.audit.api.dto.response;

import java.util.List;

public record AuditRecordsPageResponse(
		List<AuditRecordResponse> content,
		long totalElements,
		int page,
		int size
) {
}
