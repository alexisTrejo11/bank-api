package io.github.alexistrejo11.bank.accounts.domain.command;

import io.github.alexistrejo11.bank.shared.ids.UserId;

public record CreateLoanBookkeepingAccountCommand(UserId borrowerId, String currencyCode) {
}
