package io.github.alexistrejo11.bank.accounts.application;

import io.github.alexistrejo11.bank.accounts.application.command.CreateLoanBookkeepingAccountCommand;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.AccountId;

public interface CreateLoanBookkeepingAccountUseCase {

	AccountId execute(CreateLoanBookkeepingAccountCommand command);
}
