package io.github.alexistrejo11.bank.loans.api.dto.response;

import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import java.util.UUID;

public record PayRepaymentResponse(UUID repaymentId, RepaymentStatus repaymentStatus, LoanStatus loanStatus) {
}
