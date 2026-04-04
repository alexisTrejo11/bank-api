package io.github.alexistrejo11.bank.shared.exception;

import java.util.Objects;

/**
 * Base for recoverable domain and API errors mapped to HTTP by {@link io.github.alexistrejo11.bank.shared.web.GlobalExceptionHandler}.
 */
public abstract class BankException extends RuntimeException {

	private final String errorCode;

	protected BankException(String errorCode, String message) {
		super(message);
		this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
	}

	protected BankException(String errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
	}

	public String getErrorCode() {
		return errorCode;
	}
}
