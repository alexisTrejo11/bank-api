package io.github.alexistrejo11.bank.loans.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record OriginateLoanRequest(
		@NotNull UUID checkingAccountId,
		@NotNull @Positive BigDecimal principal,
		@NotBlank String currency,
		@NotNull @DecimalMin("0.0") BigDecimal monthlyInterestRate,
		@NotNull @Min(1) @Max(360) Integer termMonths
) {
}
