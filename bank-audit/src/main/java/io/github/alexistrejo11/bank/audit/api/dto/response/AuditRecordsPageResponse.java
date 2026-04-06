package io.github.alexistrejo11.bank.audit.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated audit log.")
public record AuditRecordsPageResponse(
		@Schema(description = "Page content") List<AuditRecordResponse> content,
		@Schema(description = "Total matching rows") long totalElements,
		@Schema(description = "Zero-based page") int page,
		@Schema(description = "Page size") int size
) {
}
