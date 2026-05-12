package io.github.alexistrejo11.bank.accounts.application.command;

import io.github.alexistrejo11.bank.accounts.domain.exception.InvalidTransferException;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.AccountId;
import java.math.BigDecimal;
import java.util.UUID;

public record PostTransferToLedgerCommand(
		AccountId from,
		AccountId to,
		BigDecimal amount,
		String currencyCode,
		String referenceType,
		UUID referenceId) {

	public PostTransferToLedgerCommand {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidTransferException("Amount must be positive");
		}
	}
}
