package io.github.alexistrejo11.bank.shared.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applies a Redis-backed token-bucket limit to a controller or handler method.
 * Method-level annotation overrides class-level when both are present.
 */
@Documented
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

	RateLimitProfile profile() default RateLimitProfile.STANDARD;

	RateLimitScope scope() default RateLimitScope.PER_IP;
}
