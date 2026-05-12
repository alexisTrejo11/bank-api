package io.github.alexistrejo11.bank.iam.domain.exception;

import io.github.alexistrejo11.bank.shared.shared_kernel.exception.BankException;

public class EmailAlreadyRegisteredException extends BankException {

	public EmailAlreadyRegisteredException(String email) {
		super("EMAIL_ALREADY_REGISTERED", "Email is already registered: " + email);
	}
}
