package io.github.alexistrejo11.bank.loans.domain.command;

import java.math.BigDecimal;
import java.util.UUID;

public record OriginateLoanCommand(
		UUID checkingAccountId,
		BigDecimal principal,
		String currency,
		BigDecimal monthlyInterestRate,
		int termMonths
) {
}
