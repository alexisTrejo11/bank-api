package io.github.alexistrejo11.bank.shared.event;

import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.TransferId;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Published by the payments module when a transfer settles. Accounts module listens to post ledger entries.
 */
public final class TransferCompletedEvent extends BankDomainEvent {

	private final TransferId transferId;
	private final AccountId sourceAccountId;
	private final AccountId targetAccountId;
	private final BigDecimal amount;
	private final String currencyCode;

	public TransferCompletedEvent(
			TransferId transferId,
			AccountId sourceAccountId,
			AccountId targetAccountId,
			BigDecimal amount,
			String currencyCode
	) {
		this.transferId = Objects.requireNonNull(transferId);
		this.sourceAccountId = Objects.requireNonNull(sourceAccountId);
		this.targetAccountId = Objects.requireNonNull(targetAccountId);
		this.amount = Objects.requireNonNull(amount);
		this.currencyCode = Objects.requireNonNull(currencyCode);
	}

	public TransferId transferId() {
		return transferId;
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
