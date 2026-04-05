package io.github.alexistrejo11.bank.accounts.domain.exception;

import io.github.alexistrejo11.bank.shared.exception.BankException;

public class InvalidTransferException extends BankException {

	public InvalidTransferException(String message) {
		super("INVALID_TRANSFER", message);
	}
}
