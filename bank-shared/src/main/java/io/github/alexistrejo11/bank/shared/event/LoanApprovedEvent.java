package io.github.alexistrejo11.bank.shared.event;

import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.LoanId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.math.BigDecimal;
import java.util.Objects;

public final class LoanApprovedEvent extends BankDomainEvent {

	private final LoanId loanId;
	private final UserId borrowerId;
	private final AccountId checkingAccountId;
	private final AccountId loanBookkeepingAccountId;
	private final BigDecimal principal;
	private final String currencyCode;
	private final BigDecimal monthlyInterestRate;
	private final int termMonths;

	public LoanApprovedEvent(
			LoanId loanId,
			UserId borrowerId,
			AccountId checkingAccountId,
			AccountId loanBookkeepingAccountId,
			BigDecimal principal,
			String currencyCode,
			BigDecimal monthlyInterestRate,
			int termMonths
	) {
		this.loanId = Objects.requireNonNull(loanId);
		this.borrowerId = Objects.requireNonNull(borrowerId);
		this.checkingAccountId = Objects.requireNonNull(checkingAccountId);
		this.loanBookkeepingAccountId = Objects.requireNonNull(loanBookkeepingAccountId);
		this.principal = Objects.requireNonNull(principal);
		this.currencyCode = Objects.requireNonNull(currencyCode);
		this.monthlyInterestRate = Objects.requireNonNull(monthlyInterestRate);
		this.termMonths = termMonths;
	}

	public LoanId loanId() {
		return loanId;
	}

	public UserId borrowerId() {
		return borrowerId;
	}

	public AccountId checkingAccountId() {
		return checkingAccountId;
	}

	public AccountId loanBookkeepingAccountId() {
		return loanBookkeepingAccountId;
	}

	public BigDecimal principal() {
		return principal;
	}

	public String currencyCode() {
		return currencyCode;
	}

	public BigDecimal monthlyInterestRate() {
		return monthlyInterestRate;
	}

	public int termMonths() {
		return termMonths;
	}
}
