package io.github.alexistrejo11.bank.shared.ids;

import java.util.Objects;
import java.util.UUID;

public record AccountId(UUID value) {

	public AccountId {
		Objects.requireNonNull(value, "value");
	}

	public static AccountId of(UUID id) {
		return new AccountId(id);
	}

	public static AccountId random() {
		return new AccountId(UUID.randomUUID());
	}
}
