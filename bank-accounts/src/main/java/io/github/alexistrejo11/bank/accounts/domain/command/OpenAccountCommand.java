package io.github.alexistrejo11.bank.accounts.domain.command;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.github.alexistrejo11.bank.shared.ids.UserId;

public record OpenAccountCommand(UserId ownerId, AccountType type, String currency) {
}
