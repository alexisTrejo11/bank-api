package io.github.alexistrejo11.bank.loans.domain.query;

import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.util.UUID;

public record GetLoanDetailQuery(UserId userId, UUID loanId) {
}
