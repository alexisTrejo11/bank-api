package io.github.alexistrejo11.bank.payments.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Move funds between accounts; requires Idempotency-Key header (UUID).")
public record TransferFundsRequest(
		@Schema(description = "Debit this account (must be owned by caller)") @NotNull UUID sourceAccountId,
		@Schema(description = "Credit this account") @NotNull UUID targetAccountId,
		@Schema(description = "Positive amount in account currency") @NotNull @Positive BigDecimal amount,
		@Schema(example = "USD") @NotBlank String currency
) {
}
