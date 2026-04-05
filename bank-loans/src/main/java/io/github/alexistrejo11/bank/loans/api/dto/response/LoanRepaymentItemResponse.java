package io.github.alexistrejo11.bank.loans.api.dto.response;

import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LoanRepaymentItemResponse(
		UUID repaymentId,
		int installmentNumber,
		LocalDate dueDate,
		BigDecimal amount,
		RepaymentStatus status,
		Instant paidAt
) {
}
