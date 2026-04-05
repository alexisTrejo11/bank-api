package io.github.alexistrejo11.bank.loans.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LoanAggregate(
		UUID id,
		UUID userId,
		UUID checkingAccountId,
		UUID loanAccountId,
		BigDecimal principal,
		String currency,
		BigDecimal monthlyInterestRate,
		int termMonths,
		BigDecimal monthlyPayment,
		LoanStatus status,
		Instant createdAt,
		Instant updatedAt,
		List<LoanRepaymentLine> repayments
) {

	public LoanAggregate withApproval(UUID loanBookAccountId, Instant now) {
		return new LoanAggregate(
				id, userId, checkingAccountId, loanBookAccountId, principal, currency,
				monthlyInterestRate, termMonths, monthlyPayment, LoanStatus.ACTIVE,
				createdAt, now, repayments
		);
	}

	public LoanAggregate withRepaymentsAndStatus(List<LoanRepaymentLine> newRepayments, LoanStatus newStatus, Instant now) {
		return new LoanAggregate(
				id, userId, checkingAccountId, loanAccountId, principal, currency,
				monthlyInterestRate, termMonths, monthlyPayment, newStatus,
				createdAt, now, newRepayments
		);
	}
}
