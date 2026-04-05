package io.github.alexistrejo11.bank.payments.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferFundsRequest(
		@NotNull UUID sourceAccountId,
		@NotNull UUID targetAccountId,
		@NotNull @Positive BigDecimal amount,
		@NotBlank String currency
) {
}
