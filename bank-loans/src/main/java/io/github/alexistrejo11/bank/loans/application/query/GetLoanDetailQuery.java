package io.github.alexistrejo11.bank.loans.application.query;

import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.util.UUID;

public record GetLoanDetailQuery(UserId userId, UUID loanId) {
}
