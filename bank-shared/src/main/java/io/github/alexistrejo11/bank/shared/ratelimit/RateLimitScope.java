package io.github.alexistrejo11.bank.shared.ratelimit;

/**
 * Identity used for rate-limit keying.
 */
public enum RateLimitScope {

	/** Client IP (supports {@code X-Forwarded-For} when present). */
	PER_IP,

	/** Authenticated user id; falls back to per-IP when anonymous. */
	PER_USER
}
