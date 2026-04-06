package io.github.alexistrejo11.bank.loans.domain.exception;

import io.github.alexistrejo11.bank.shared.exception.BankException;

public class LoanException extends BankException {

	public LoanException(String errorCode, String message) {
		super(errorCode, message);
	}
}
