package io.github.alexistrejo11.bank.payments.domain.model;

public enum TransferStatus {
	PENDING,
	PROCESSING,
	COMPLETED,
	FAILED,
	REVERSED
}
