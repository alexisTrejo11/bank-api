package io.github.alexistrejo11.bank.accounts.application.command;

import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;

public record CreateLoanBookkeepingAccountCommand(UserId borrowerId, String currencyCode) {
}
