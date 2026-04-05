package io.github.alexistrejo11.bank.shared.event;

import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.LoanId;
import io.github.alexistrejo11.bank.shared.ids.LoanRepaymentId;
import java.math.BigDecimal;
import java.util.Objects;

public final class LoanRepaymentCompletedEvent extends BankDomainEvent {

	private final LoanId loanId;
	private final LoanRepaymentId repaymentId;
	private final AccountId checkingAccountId;
	private final BigDecimal amount;
	private final String currencyCode;

	public LoanRepaymentCompletedEvent(
			LoanId loanId,
			LoanRepaymentId repaymentId,
			AccountId checkingAccountId,
			BigDecimal amount,
			String currencyCode
	) {
		this.loanId = Objects.requireNonNull(loanId);
		this.repaymentId = Objects.requireNonNull(repaymentId);
		this.checkingAccountId = Objects.requireNonNull(checkingAccountId);
		this.amount = Objects.requireNonNull(amount);
		this.currencyCode = Objects.requireNonNull(currencyCode);
	}

	public LoanId loanId() {
		return loanId;
	}

	public LoanRepaymentId repaymentId() {
		return repaymentId;
	}

	public AccountId checkingAccountId() {
		return checkingAccountId;
	}

	public BigDecimal amount() {
		return amount;
	}

	public String currencyCode() {
		return currencyCode;
	}
}
