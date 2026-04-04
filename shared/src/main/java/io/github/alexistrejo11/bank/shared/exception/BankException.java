package io.github.alexistrejo11.bank.shared.exception;

import java.util.Objects;

/**
 * Expected domain/API failure with a stable {@link #getErrorCode()}. Mapped to HTTP by
 * {@link io.github.alexistrejo11.bank.shared.web.GlobalExceptionHandler}; use {@code Result} in handlers when a failure is a normal outcome.
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

	/** Stable code for clients and logs (e.g. {@code ACCOUNT_NOT_FOUND}). */
	public String getErrorCode() {
		return errorCode;
	}
}
