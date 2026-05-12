package io.github.alexistrejo11.bank.accounts.application;

import io.github.alexistrejo11.bank.accounts.application.command.PostTransferToLedgerCommand;

public interface PostTransferToLedgerUseCase {

	void execute(PostTransferToLedgerCommand command);
}
