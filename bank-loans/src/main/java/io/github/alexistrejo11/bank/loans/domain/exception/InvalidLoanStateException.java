package io.github.alexistrejo11.bank.loans.domain.exception;

import io.github.alexistrejo11.bank.shared.exception.BankException;

public class InvalidLoanStateException extends BankException {

	public InvalidLoanStateException(String message) {
		super("LOAN_INVALID_STATE", message);
	}
}
