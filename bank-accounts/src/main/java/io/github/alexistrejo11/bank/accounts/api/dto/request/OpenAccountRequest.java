package io.github.alexistrejo11.bank.accounts.api.dto.request;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Open a customer account in the given ISO currency.")
public record OpenAccountRequest(
		@Schema(description = "CHECKING or SAVINGS") @NotNull AccountType type,
		@Schema(description = "ISO 4217 alphabetic code", example = "USD") @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "[A-Za-z]{3}") String currency
) {
}
