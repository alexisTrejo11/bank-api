package io.github.alexistrejo11.bank.accounts.domain.port.in.command;

import io.github.alexistrejo11.bank.accounts.domain.command.PostTransferToLedgerCommand;

public interface PostTransferToLedgerUseCase {

	void execute(PostTransferToLedgerCommand command);
}
