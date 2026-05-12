package io.github.alexistrejo11.bank.accounts.presentation.dto.response;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Newly opened account identifiers.")
public record OpenAccountResponse(
		@Schema(description = "Account id used in URLs and transfers") UUID accountId,
		@Schema(example = "USD") String currency,
		@Schema(description = "CHECKING or SAVINGS or INTERNAL or LOAN") AccountType type) {
}
