package io.github.alexistrejo11.bank.loans.application.command;

import java.util.UUID;

public record ApproveLoanCommand(UUID loanId) {
}
