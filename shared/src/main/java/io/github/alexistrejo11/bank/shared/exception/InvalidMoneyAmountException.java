package io.github.alexistrejo11.bank.shared.exception;

import java.math.BigDecimal;

public class InvalidMoneyAmountException extends BankException {

	public InvalidMoneyAmountException(BigDecimal amount) {
		super("INVALID_MONEY_AMOUNT", "Amount must be positive, got: " + amount);
	}
}
