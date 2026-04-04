package io.github.alexistrejo11.bank.shared.exception;

/** Maps to HTTP 404 via {@link io.github.alexistrejo11.bank.shared.web.GlobalExceptionHandler}. */
public class ResourceNotFoundException extends BankException {

	public ResourceNotFoundException(String errorCode, String message) {
		super(errorCode, message);
	}
}
