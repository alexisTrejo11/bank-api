package io.github.alexistrejo11.bank.payments.application.command;

import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.math.BigDecimal;
import java.util.UUID;

public record InitiateTransferCommand(
		UserId userId,
		UUID idempotencyKey,
		UUID sourceAccountId,
		UUID targetAccountId,
		BigDecimal amount,
		String currencyRaw) {
}
