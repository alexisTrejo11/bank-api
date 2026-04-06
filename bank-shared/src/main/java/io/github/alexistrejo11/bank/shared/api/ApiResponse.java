package io.github.alexistrejo11.bank.shared.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Standard JSON body for REST APIs: {@code data} on success, {@code errors} when the call failed logically.
 *
 * @param <T> type of {@code data}
 */
@Schema(description = "Standard response envelope: `data` on success; `errors` when the request failed logically (e.g. validation or domain rules).")
public record ApiResponse<T>(
		@Schema(description = "Success payload; null when `errors` is non-empty.") T data,
		@Schema(description = "Response metadata (server time, optional request id).") Meta meta,
		@Schema(description = "Logical errors; empty on success.") List<ApiError> errors
) {

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

	/** {@code true} when {@code errors} is empty. */
	public boolean isSuccess() {
		return errors.isEmpty();
	}

	/** Response metadata (server time; optional correlation id). */
	@Schema(name = "ApiResponseMeta", description = "Server timestamp and optional correlation id.")
	public record Meta(
			@Schema(description = "UTC instant when the response was built.") Instant timestamp,
			@Schema(description = "Optional request correlation id from headers or gateway.") String requestId
	) {

		public Meta {
			Objects.requireNonNull(timestamp, "timestamp");
		}
	}

	/** Single logical error; {@code field} set when validation targets one input. */
	@Schema(name = "ApiError", description = "Single logical error code and message.")
	public record ApiError(
			@Schema(description = "Stable machine-readable code (e.g. INSUFFICIENT_FUNDS).") String code,
			@Schema(description = "Human-readable explanation.") String message,
			@Schema(description = "When validation targets one field, its JSON path or name.") String field
	) {

		public ApiError {
			Objects.requireNonNull(code, "code");
			Objects.requireNonNull(message, "message");
		}
	}
}
