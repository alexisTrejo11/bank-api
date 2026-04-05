package io.github.alexistrejo11.bank.accounts.api.dto.response;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import java.util.UUID;

public record OpenAccountResponse(UUID accountId, String currency, AccountType type) {
}
