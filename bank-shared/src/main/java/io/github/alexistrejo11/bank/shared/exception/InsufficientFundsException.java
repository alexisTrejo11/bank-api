package io.github.alexistrejo11.bank.shared.exception;

/** Thrown when a debit would leave an invalid or negative balance for the modeled operation. */
public class InsufficientFundsException extends BankException {

	public InsufficientFundsException(String message) {
		super("INSUFFICIENT_FUNDS", message);
	}
}
