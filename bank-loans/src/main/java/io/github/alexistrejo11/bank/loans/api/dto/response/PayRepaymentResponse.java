package io.github.alexistrejo11.bank.loans.api.dto.response;

import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Result of paying one installment.")
public record PayRepaymentResponse(
		@Schema(description = "Installment id") UUID repaymentId,
		@Schema(description = "Should be PAID on success") RepaymentStatus repaymentStatus,
		@Schema(description = "ACTIVE or PAID_OFF when last installment is paid") LoanStatus loanStatus
) {
}
