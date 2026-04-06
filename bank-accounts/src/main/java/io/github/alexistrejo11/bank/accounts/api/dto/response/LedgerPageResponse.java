package io.github.alexistrejo11.bank.accounts.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated ledger slice (newest first in the handler).")
public record LedgerPageResponse(
		@Schema(description = "Page content") List<LedgerEntryResponse> content,
		@Schema(description = "Total rows across pages") long totalElements,
		@Schema(description = "Zero-based page index") int page,
		@Schema(description = "Page size") int size
) {
}
