package io.github.alexistrejo11.bank.accounts.domain.exception;

import io.github.alexistrejo11.bank.shared.exception.ResourceNotFoundException;

public class AccountNotFoundException extends ResourceNotFoundException {

	public AccountNotFoundException(String message) {
		super("ACCOUNT_NOT_FOUND", message);
	}
}
