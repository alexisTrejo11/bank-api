package io.github.alexistrejo11.bank.iam.infrastructure.security;

import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.iam.redis.enabled", havingValue = "true")
public class RedisJwtBlocklistStore implements JwtBlocklistStore {

	private final StringRedisTemplate redis;

	public RedisJwtBlocklistStore(StringRedisTemplate redis) {
		this.redis = redis;
	}

	@Override
	public void add(String jti, Duration ttl) {
		if (ttl.isZero() || ttl.isNegative()) {
			return;
		}
		redis.opsForValue().set("iam:blocklist:" + jti, "1", ttl);
	}

	@Override
	public boolean isBlocked(String jti) {
		return Boolean.TRUE.equals(redis.hasKey("iam:blocklist:" + jti));
	}
}
