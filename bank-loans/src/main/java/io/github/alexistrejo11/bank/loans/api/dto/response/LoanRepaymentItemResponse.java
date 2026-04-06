package io.github.alexistrejo11.bank.loans.api.dto.response;

import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "One installment in the amortization schedule.")
public record LoanRepaymentItemResponse(
		@Schema(description = "Repayment id (used in pay endpoint)") UUID repaymentId,
		@Schema(description = "1-based installment index") int installmentNumber,
		@Schema(description = "Due date (UTC calendar)") LocalDate dueDate,
		@Schema(description = "Payment due for this period") BigDecimal amount,
		@Schema(description = "PENDING or PAID") RepaymentStatus status,
		@Schema(description = "When marked paid (UTC)") Instant paidAt
) {
}
