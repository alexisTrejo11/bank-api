package io.github.alexistrejo11.bank.iam.infrastructure.security;

import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.iam.redis.enabled", havingValue = "true")
public class RedisRefreshTokenStore implements RefreshTokenStore {

	private final StringRedisTemplate redis;

	public RedisRefreshTokenStore(StringRedisTemplate redis) {
		this.redis = redis;
	}

	@Override
	public void store(String refreshToken, UserId userId, Duration ttl) {
		String key = "iam:refresh:" + Sha256.hex(refreshToken);
		redis.opsForValue().set(key, userId.value().toString(), ttl);
	}

	@Override
	public Optional<UserId> findUserId(String refreshToken) {
		String key = "iam:refresh:" + Sha256.hex(refreshToken);
		String v = redis.opsForValue().get(key);
		if (v == null || v.isBlank()) {
			return Optional.empty();
		}
		return Optional.of(UserId.of(UUID.fromString(v)));
	}

	@Override
	public void remove(String refreshToken) {
		redis.delete("iam:refresh:" + Sha256.hex(refreshToken));
	}
}
