package io.github.alexistrejo11.bank.shared.ids;

import java.util.Objects;
import java.util.UUID;

/** Identifier for a loan aggregate. */
public record LoanId(UUID value) {

	public LoanId {
		Objects.requireNonNull(value, "value");
	}

	public static LoanId of(UUID id) {
		return new LoanId(id);
	}

	public static LoanId random() {
		return new LoanId(UUID.randomUUID());
	}
}
