package io.github.alexistrejo11.bank.infrastructure.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Infrastructure-wide protection for {@code /api/**}: per-IP token bucket stricter than typical SaaS defaults.
 */
public final class GlobalRateLimitFilter extends OncePerRequestFilter {

	private static final String PREFIX = "ratelimit:global:v1:ip:";

	private final RedisTokenBucketRateLimiter rateLimiter;
	private final RateLimitingProperties properties;

	public GlobalRateLimitFilter(RedisTokenBucketRateLimiter rateLimiter, RateLimitingProperties properties) {
		this.rateLimiter = rateLimiter;
		this.properties = properties;
	}

	@Override
	protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
		String path = request.getRequestURI();
		if (path == null) {
			return true;
		}
		return !path.startsWith("/api/")
				|| path.startsWith("/api/actuator")
				|| path.startsWith("/actuator");
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		String ip = ClientIpResolver.resolve(request);
		String key = PREFIX + ip;
		var g = properties.getGlobal();
		RateLimitDecision d = rateLimiter.tryConsume(key, g.getCapacity(), g.getRefillPerSecond());
		if (!d.allowed()) {
			RateLimitResponseWriter.writeTooManyRequests(
					response,
					d.retryAfterSeconds(),
					(long) g.getCapacity(),
					0L
			);
			return;
		}
		response.setHeader("X-RateLimit-Limit", Long.toString(g.getCapacity()));
		response.setHeader("X-RateLimit-Remaining", Long.toString(d.remainingTokens()));
		filterChain.doFilter(request, response);
	}
}
