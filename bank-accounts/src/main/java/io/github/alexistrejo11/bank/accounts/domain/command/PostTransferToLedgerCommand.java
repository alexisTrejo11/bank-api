package io.github.alexistrejo11.bank.accounts.domain.command;

import io.github.alexistrejo11.bank.shared.ids.AccountId;
import java.math.BigDecimal;
import java.util.UUID;

public record PostTransferToLedgerCommand(
		AccountId from,
		AccountId to,
		BigDecimal amount,
		String currencyCode,
		String referenceType,
		UUID referenceId
) {
}
