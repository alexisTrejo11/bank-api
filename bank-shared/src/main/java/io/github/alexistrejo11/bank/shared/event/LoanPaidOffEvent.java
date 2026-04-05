package io.github.alexistrejo11.bank.shared.event;

import io.github.alexistrejo11.bank.shared.ids.LoanId;
import java.util.Objects;

public final class LoanPaidOffEvent extends BankDomainEvent {

	private final LoanId loanId;

	public LoanPaidOffEvent(LoanId loanId) {
		this.loanId = Objects.requireNonNull(loanId);
	}

	public LoanId loanId() {
		return loanId;
	}
}
