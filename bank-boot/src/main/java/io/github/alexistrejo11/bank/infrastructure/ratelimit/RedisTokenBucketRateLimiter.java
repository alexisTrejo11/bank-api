package io.github.alexistrejo11.bank.infrastructure.ratelimit;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * Atomic token-bucket using a Lua script (refill + consume in one round trip).
 */
public class RedisTokenBucketRateLimiter {

	private static final Logger log = LoggerFactory.getLogger(RedisTokenBucketRateLimiter.class);

	private static final DefaultRedisScript<List> SCRIPT = new DefaultRedisScript<>();

	static {
		SCRIPT.setLocation(new ClassPathResource("redis/ratelimit-token-bucket.lua"));
		SCRIPT.setResultType(List.class);
	}

	private final StringRedisTemplate redis;
	private final RateLimitingProperties properties;

	public RedisTokenBucketRateLimiter(StringRedisTemplate redis, RateLimitingProperties properties) {
		this.redis = redis;
		this.properties = properties;
	}

	public RateLimitDecision tryConsume(String redisKey, int capacity, double refillPerSecond) {
		try {
			long now = System.currentTimeMillis();
			@SuppressWarnings("unchecked")
			List<Long> raw = redis.execute(
					SCRIPT,
					Collections.singletonList(redisKey),
					List.of(
							Integer.toString(capacity),
							Double.toString(refillPerSecond),
							Long.toString(now)
					)
			);
			if (raw == null || raw.size() < 3) {
				return failOrAllow(capacity, "rate_limit_empty_script_result");
			}
			long allowed = raw.get(0);
			long remaining = raw.get(1);
			long retryAfter = raw.get(2);
			if (allowed == 1L) {
				return RateLimitDecision.allowed(remaining);
			}
			return RateLimitDecision.denied((int) retryAfter);
		}
		catch (Exception e) {
			log.warn("rate_limit_redis_error key={}", redisKey, e);
			return failOrAllow(capacity, "rate_limit_redis_exception");
		}
	}

	private RateLimitDecision failOrAllow(int capacity, String reason) {
		if (properties.isFailOpen()) {
			log.debug("{} — allowing request (fail-open)", reason);
			return RateLimitDecision.allowed(capacity);
		}
		return RateLimitDecision.denied(60);
	}
}
