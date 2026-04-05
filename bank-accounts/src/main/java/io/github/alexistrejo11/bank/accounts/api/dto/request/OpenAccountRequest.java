package io.github.alexistrejo11.bank.accounts.api.dto.request;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OpenAccountRequest(
		@NotNull AccountType type,
		@NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "[A-Za-z]{3}") String currency
) {
}
