package io.github.alexistrejo11.bank.iam.domain.exception;

import io.github.alexistrejo11.bank.shared.shared_kernel.exception.BankException;

public class InvalidCredentialsException extends BankException {

	public InvalidCredentialsException() {
		super("INVALID_CREDENTIALS", "Invalid email or password");
	}
}
