package io.github.alexistrejo11.bank.iam.exception;

import io.github.alexistrejo11.bank.shared.exception.BankException;

public class InvalidCredentialsException extends BankException {

	public InvalidCredentialsException() {
		super("INVALID_CREDENTIALS", "Invalid email or password");
	}
}
