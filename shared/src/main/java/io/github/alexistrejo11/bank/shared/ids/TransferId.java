package io.github.alexistrejo11.bank.shared.ids;

import java.util.Objects;
import java.util.UUID;

/** Identifier for a payment transfer. */
public record TransferId(UUID value) {

	public TransferId {
		Objects.requireNonNull(value, "value");
	}

	public static TransferId of(UUID id) {
		return new TransferId(id);
	}

	public static TransferId random() {
		return new TransferId(UUID.randomUUID());
	}
}
