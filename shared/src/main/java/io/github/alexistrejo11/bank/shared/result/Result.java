package io.github.alexistrejo11.bank.shared.result;

import java.util.Objects;

public sealed interface Result<T> permits Result.Success, Result.Failure {

	record Success<T>(T value) implements Result<T> {

		public Success {
			Objects.requireNonNull(value, "value");
		}
	}

	record Failure<T>(String code, String message) implements Result<T> {

		public Failure {
			Objects.requireNonNull(code, "code");
			Objects.requireNonNull(message, "message");
		}
	}

	static <T> Result<T> success(T value) {
		return new Success<>(value);
	}

	static <T> Result<T> failure(String code, String message) {
		return new Failure<>(code, message);
	}

	default boolean isSuccess() {
		return this instanceof Success;
	}

	default boolean isFailure() {
		return this instanceof Failure;
	}
}
