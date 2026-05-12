package io.github.alexistrejo11.bank.loans.domain.exception;

import io.github.alexistrejo11.bank.shared.shared_kernel.exception.BankException;

public class InvalidLoanStateException extends BankException {

	public InvalidLoanStateException(String message) {
		super("LOAN_INVALID_STATE", message);
	}
}
