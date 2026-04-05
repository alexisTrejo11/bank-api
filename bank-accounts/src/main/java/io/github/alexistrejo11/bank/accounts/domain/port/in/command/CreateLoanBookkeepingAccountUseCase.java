package io.github.alexistrejo11.bank.accounts.domain.port.in.command;

import io.github.alexistrejo11.bank.accounts.domain.command.CreateLoanBookkeepingAccountCommand;
import io.github.alexistrejo11.bank.shared.ids.AccountId;

public interface CreateLoanBookkeepingAccountUseCase {

	AccountId execute(CreateLoanBookkeepingAccountCommand command);
}
