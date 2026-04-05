package io.github.alexistrejo11.bank.payments.infrastructure.idempotency;

import io.github.alexistrejo11.bank.payments.domain.port.out.TransferIdempotencyPort;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.payments.redis-idempotency", havingValue = "true")
public class RedisTransferIdempotencyAdapter implements TransferIdempotencyPort {

	private static final Duration TTL = Duration.ofHours(24);

	private final StringRedisTemplate redis;

	public RedisTransferIdempotencyAdapter(StringRedisTemplate redis) {
		this.redis = redis;
	}

	@Override
	public Optional<String> getCachedJson(UserId userId, UUID idempotencyKey) {
		String v = redis.opsForValue().get(cacheKey(userId, idempotencyKey));
		return Optional.ofNullable(v);
	}

	@Override
	public void putCachedJson(UserId userId, UUID idempotencyKey, String json) {
		redis.opsForValue().set(cacheKey(userId, idempotencyKey), json, TTL);
	}

	private static String cacheKey(UserId userId, UUID idempotencyKey) {
		return "payments:idempotency:" + userId.value() + ":" + idempotencyKey;
	}
}
