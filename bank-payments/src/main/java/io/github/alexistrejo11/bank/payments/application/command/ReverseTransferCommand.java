package io.github.alexistrejo11.bank.payments.application.command;

import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.util.UUID;

public record ReverseTransferCommand(
		UserId userId,
		UUID idempotencyKey,
		UUID originalTransferId) {
}
