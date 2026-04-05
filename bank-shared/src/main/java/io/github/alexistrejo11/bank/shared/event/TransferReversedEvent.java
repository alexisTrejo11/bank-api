package io.github.alexistrejo11.bank.shared.event;

import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.TransferId;
import java.math.BigDecimal;
import java.util.Objects;

/** Published when a reversal transfer settles (original transfer marked reversed). */
public final class TransferReversedEvent extends BankDomainEvent {

	private final TransferId reversalTransferId;
	private final TransferId originalTransferId;
	private final AccountId sourceAccountId;
	private final AccountId targetAccountId;
	private final BigDecimal amount;
	private final String currencyCode;

	public TransferReversedEvent(
			TransferId reversalTransferId,
			TransferId originalTransferId,
			AccountId sourceAccountId,
			AccountId targetAccountId,
			BigDecimal amount,
			String currencyCode
	) {
		this.reversalTransferId = Objects.requireNonNull(reversalTransferId);
		this.originalTransferId = Objects.requireNonNull(originalTransferId);
		this.sourceAccountId = Objects.requireNonNull(sourceAccountId);
		this.targetAccountId = Objects.requireNonNull(targetAccountId);
		this.amount = Objects.requireNonNull(amount);
		this.currencyCode = Objects.requireNonNull(currencyCode);
	}

	public TransferId reversalTransferId() {
		return reversalTransferId;
	}

	public TransferId originalTransferId() {
		return originalTransferId;
	}

	public AccountId sourceAccountId() {
		return sourceAccountId;
	}

	public AccountId targetAccountId() {
		return targetAccountId;
	}

	public BigDecimal amount() {
		return amount;
	}

	public String currencyCode() {
		return currencyCode;
	}
}
