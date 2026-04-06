package io.github.alexistrejo11.bank.loans.domain.command;

import java.util.UUID;

public record ApproveLoanCommand(UUID loanId) {
}
