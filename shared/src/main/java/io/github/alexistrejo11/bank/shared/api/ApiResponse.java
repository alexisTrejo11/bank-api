package io.github.alexistrejo11.bank.shared.api;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record ApiResponse<T>(T data, Meta meta, List<ApiError> errors) {

	public ApiResponse {
		meta = Objects.requireNonNull(meta, "meta");
		errors = errors == null ? List.of() : List.copyOf(errors);
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(data, new Meta(Instant.now(), null), List.of());
	}

	public static <T> ApiResponse<T> success(T data, String requestId) {
		return new ApiResponse<>(data, new Meta(Instant.now(), requestId), List.of());
	}

	public static <T> ApiResponse<T> failure(String code, String message) {
		return new ApiResponse<>(null, new Meta(Instant.now(), null), List.of(new ApiError(code, message, null)));
	}

	public static <T> ApiResponse<T> failure(String code, String message, String requestId) {
		return new ApiResponse<>(null, new Meta(Instant.now(), requestId), List.of(new ApiError(code, message, null)));
	}

	public boolean isSuccess() {
		return errors.isEmpty();
	}

	public record Meta(Instant timestamp, String requestId) {

		public Meta {
			Objects.requireNonNull(timestamp, "timestamp");
		}
	}

	public record ApiError(String code, String message, String field) {

		public ApiError {
			Objects.requireNonNull(code, "code");
			Objects.requireNonNull(message, "message");
		}
	}
}
