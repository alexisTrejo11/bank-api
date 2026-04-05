package io.github.alexistrejo11.bank.loans.api.dto.response;

import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LoanDetailResponse(
		UUID loanId,
		UUID checkingAccountId,
		UUID loanBookkeepingAccountId,
		BigDecimal principal,
		String currency,
		BigDecimal monthlyInterestRate,
		int termMonths,
		BigDecimal monthlyPayment,
		LoanStatus status,
		Instant createdAt,
		List<LoanRepaymentItemResponse> repayments
) {
}
