package io.github.alexistrejo11.bank.loans.api.dto.response;

import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Loan header, terms, and repayment schedule lines.")
public record LoanDetailResponse(
		@Schema(description = "Loan id") UUID loanId,
		@Schema(description = "Customer checking account") UUID checkingAccountId,
		@Schema(description = "Internal LOAN-type bookkeeping account (after approval)") UUID loanBookkeepingAccountId,
		@Schema(description = "Original principal") BigDecimal principal,
		@Schema(example = "USD") String currency,
		@Schema(description = "Monthly interest rate decimal") BigDecimal monthlyInterestRate,
		@Schema(description = "Term in months") int termMonths,
		@Schema(description = "Fixed monthly installment") BigDecimal monthlyPayment,
		@Schema(description = "PENDING_APPROVAL, ACTIVE, PAID_OFF, DEFAULTED") LoanStatus status,
		@Schema(description = "Creation time (UTC)") Instant createdAt,
		@Schema(description = "Installment schedule") List<LoanRepaymentItemResponse> repayments
) {
}
