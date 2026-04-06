package io.github.alexistrejo11.bank.infrastructure.ratelimit;

import io.github.alexistrejo11.bank.iam.infrastructure.security.IamUserPrincipal;
import io.github.alexistrejo11.bank.shared.ratelimit.RateLimit;
import io.github.alexistrejo11.bank.shared.ratelimit.RateLimitProfile;
import io.github.alexistrejo11.bank.shared.ratelimit.RateLimitScope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Applies {@link RateLimit} from handler method or declaring controller class.
 */
public final class AnnotatedRateLimitInterceptor implements HandlerInterceptor {

	private static final String KEY_PREFIX = "ratelimit:ann:v1:";

	private final RedisTokenBucketRateLimiter rateLimiter;
	private final RateLimitingProperties properties;

	public AnnotatedRateLimitInterceptor(RedisTokenBucketRateLimiter rateLimiter, RateLimitingProperties properties) {
		this.rateLimiter = rateLimiter;
		this.properties = properties;
	}

	@Override
	public boolean preHandle(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull Object handler
	) throws Exception {
		if (!(handler instanceof HandlerMethod hm)) {
			return true;
		}
		RateLimit methodAnn = hm.getMethodAnnotation(RateLimit.class);
		RateLimit classAnn = hm.getBeanType().getAnnotation(RateLimit.class);
		RateLimit effective = methodAnn != null ? methodAnn : classAnn;
		if (effective == null) {
			return true;
		}
		RateLimitProfile profile = effective.profile();
		RateLimitScope scope = effective.scope();
		RateLimitingProperties.TokenBucketSpec spec = properties.specFor(profile);
		String identity = resolveIdentity(request, scope);
		String mapping = request.getMethod() + ":" + request.getRequestURI();
		String redisKey = KEY_PREFIX + profile.name() + ":" + scope.name() + ":" + identity + ":" + mapping;
		RateLimitDecision d = rateLimiter.tryConsume(redisKey, spec.getCapacity(), spec.getRefillPerSecond());
		if (!d.allowed()) {
			RateLimitResponseWriter.writeTooManyRequests(
					response,
					d.retryAfterSeconds(),
					(long) spec.getCapacity(),
					0L
			);
			return false;
		}
		response.setHeader("X-RateLimit-Limit-Endpoint", Long.toString(spec.getCapacity()));
		response.setHeader("X-RateLimit-Remaining-Endpoint", Long.toString(d.remainingTokens()));
		return true;
	}

	private static String resolveIdentity(HttpServletRequest request, RateLimitScope scope) {
		if (scope == RateLimitScope.PER_USER) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof IamUserPrincipal p) {
				return "u:" + p.userId().value();
			}
		}
		return "ip:" + ClientIpResolver.resolve(request);
	}
}
