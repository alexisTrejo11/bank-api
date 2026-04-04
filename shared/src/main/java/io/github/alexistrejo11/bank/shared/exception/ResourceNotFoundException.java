package io.github.alexistrejo11.bank.shared.exception;

public class ResourceNotFoundException extends BankException {

	public ResourceNotFoundException(String errorCode, String message) {
		super(errorCode, message);
	}
}
