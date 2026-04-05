package io.github.alexistrejo11.bank.shared.event;

import io.github.alexistrejo11.bank.shared.ids.TransferId;
import java.util.Objects;

/** Published when a transfer cannot be completed (validation or processing failure). */
public final class TransferFailedEvent extends BankDomainEvent {

	private final TransferId transferId;
	private final String reasonCode;
	private final String message;

	public TransferFailedEvent(TransferId transferId, String reasonCode, String message) {
		this.transferId = Objects.requireNonNull(transferId);
		this.reasonCode = Objects.requireNonNull(reasonCode);
		this.message = Objects.requireNonNull(message);
	}

	public TransferId transferId() {
		return transferId;
	}

	public String reasonCode() {
		return reasonCode;
	}

	public String message() {
		return message;
	}
}
