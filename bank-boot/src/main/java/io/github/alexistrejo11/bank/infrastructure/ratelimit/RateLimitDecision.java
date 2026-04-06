package io.github.alexistrejo11.bank.infrastructure.ratelimit;

/**
 * Outcome of a single token-bucket acquisition attempt.
 */
public record RateLimitDecision(boolean allowed, long remainingTokens, int retryAfterSeconds) {

	public static RateLimitDecision allowed(long remainingTokens) {
		return new RateLimitDecision(true, Math.max(0, remainingTokens), 0);
	}

	public static RateLimitDecision denied(int retryAfterSeconds) {
		return new RateLimitDecision(false, 0, Math.max(1, retryAfterSeconds));
	}
}
