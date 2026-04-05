package io.github.alexistrejo11.bank.shared.ids;

import java.util.Objects;
import java.util.UUID;

public record LoanRepaymentId(UUID value) {

	public LoanRepaymentId {
		Objects.requireNonNull(value, "value");
	}

	public static LoanRepaymentId of(UUID id) {
		return new LoanRepaymentId(id);
	}
}
