package io.github.alexistrejo11.bank.accounts.api.dto.response;

import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntryType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "One ledger line (half of a double-entry movement).")
public record LedgerEntryResponse(
		@Schema(description = "Ledger entry id") UUID id,
		@Schema(description = "DEBIT or CREDIT") LedgerEntryType entryType,
		@Schema(description = "Always positive amount") BigDecimal amount,
		@Schema(example = "USD") String currency,
		@Schema(description = "Domain reference, e.g. TRANSFER, LOAN_DISBURSE", example = "TRANSFER") String referenceType,
		@Schema(description = "Id of the transfer, loan, or repayment") UUID referenceId,
		@Schema(description = "Posting time (UTC)") Instant createdAt
) {
}
