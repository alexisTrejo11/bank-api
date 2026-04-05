package io.github.alexistrejo11.bank.loans.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LoanRepaymentLine(
		UUID id,
		int installmentNumber,
		LocalDate dueDate,
		BigDecimal amount,
		RepaymentStatus status,
		Instant paidAt
) {

	public LoanRepaymentLine withPaid(Instant paidAt) {
		return new LoanRepaymentLine(id, installmentNumber, dueDate, amount, RepaymentStatus.PAID, paidAt);
	}
}
