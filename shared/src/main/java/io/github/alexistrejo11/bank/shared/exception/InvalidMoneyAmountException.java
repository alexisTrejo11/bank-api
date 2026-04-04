package io.github.alexistrejo11.bank.shared.exception;

import java.math.BigDecimal;

/** Thrown when a money amount violates domain rules (e.g. non-positive where not allowed). */
public class InvalidMoneyAmountException extends BankException {

	public InvalidMoneyAmountException(BigDecimal amount) {
		super("INVALID_MONEY_AMOUNT", "Amount must be positive, got: " + amount);
	}
}
