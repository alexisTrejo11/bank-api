package io.github.alexistrejo11.bank.accounts.domain.model;

public enum AccountType {
	CHECKING,
	SAVINGS,
	/** Bank-internal funding partner for loan disbursements / repayments (not customer-facing). */
	INTERNAL,
	/** Bookkeeping account for an outstanding loan principal. */
	LOAN
}
