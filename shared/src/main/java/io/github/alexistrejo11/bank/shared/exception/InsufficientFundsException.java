package io.github.alexistrejo11.bank.shared.exception;

public class InsufficientFundsException extends BankException {

	public InsufficientFundsException(String message) {
		super("INSUFFICIENT_FUNDS", message);
	}
}
