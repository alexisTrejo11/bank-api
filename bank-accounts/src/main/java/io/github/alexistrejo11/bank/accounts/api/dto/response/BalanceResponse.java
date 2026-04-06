package io.github.alexistrejo11.bank.accounts.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Derived balance from ledger (credits minus debits).")
public record BalanceResponse(
		@Schema(description = "Current balance") BigDecimal balance,
		@Schema(example = "USD") String currency
) {
}
