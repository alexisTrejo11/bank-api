package io.github.alexistrejo11.bank.iam.exception;

import io.github.alexistrejo11.bank.shared.exception.BankException;

public class EmailAlreadyRegisteredException extends BankException {

	public EmailAlreadyRegisteredException(String email) {
		super("EMAIL_ALREADY_REGISTERED", "Email is already registered: " + email);
	}
}
