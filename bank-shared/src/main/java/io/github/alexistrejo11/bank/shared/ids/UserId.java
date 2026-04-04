package io.github.alexistrejo11.bank.shared.ids;

import java.util.Objects;
import java.util.UUID;

/** Identifier for an IAM user. */
public record UserId(UUID value) {

	public UserId {
		Objects.requireNonNull(value, "value");
	}

	public static UserId of(UUID id) {
		return new UserId(id);
	}

	public static UserId random() {
		return new UserId(UUID.randomUUID());
	}
}
