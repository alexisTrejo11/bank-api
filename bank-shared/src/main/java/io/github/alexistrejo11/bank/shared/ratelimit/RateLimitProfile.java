package io.github.alexistrejo11.bank.shared.ratelimit;

/**
 * Named rate-limit presets; numeric limits are configured in {@code bank.rate-limiting.profiles.*}.
 */
public enum RateLimitProfile {

	/** Default API traffic. */
	STANDARD,

	/** Auth and high-abuse endpoints (tighter burst + steady rate). */
	STRICT,

	/** Financial mutations (transfers, loan payments, etc.). */
	SENSITIVE_OPERATIONS
}
