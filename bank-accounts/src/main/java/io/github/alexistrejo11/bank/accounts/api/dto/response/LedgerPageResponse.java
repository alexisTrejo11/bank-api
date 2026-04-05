package io.github.alexistrejo11.bank.accounts.api.dto.response;

import java.util.List;

public record LedgerPageResponse(
		List<LedgerEntryResponse> content,
		long totalElements,
		int page,
		int size
) {
}
