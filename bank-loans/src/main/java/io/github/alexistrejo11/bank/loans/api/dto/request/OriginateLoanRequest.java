package io.github.alexistrejo11.bank.loans.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Originate a loan against an owned checking account; creates PENDING_APPROVAL and amortization schedule.")
public record OriginateLoanRequest(
		@Schema(description = "Checking account that receives disbursement and pays installments") @NotNull UUID checkingAccountId,
		@Schema(description = "Loan principal") @NotNull @Positive BigDecimal principal,
		@Schema(example = "USD") @NotBlank String currency,
		@Schema(description = "Monthly interest as decimal (e.g. 0.005 = 0.5% per month)") @NotNull @DecimalMin("0.0") BigDecimal monthlyInterestRate,
		@Schema(description = "Number of monthly installments", example = "12") @NotNull @Min(1) @Max(360) Integer termMonths
) {
}
