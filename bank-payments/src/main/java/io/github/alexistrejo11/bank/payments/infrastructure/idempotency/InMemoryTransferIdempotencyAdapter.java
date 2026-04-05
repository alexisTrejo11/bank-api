package io.github.alexistrejo11.bank.payments.infrastructure.idempotency;

import io.github.alexistrejo11.bank.payments.domain.port.out.TransferIdempotencyPort;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.payments.redis-idempotency", havingValue = "false", matchIfMissing = true)
public class InMemoryTransferIdempotencyAdapter implements TransferIdempotencyPort {

	private static final Duration TTL = Duration.ofHours(24);

	private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

	@Override
	public Optional<String> getCachedJson(UserId userId, UUID idempotencyKey) {
		String key = cacheKey(userId, idempotencyKey);
		CacheEntry e = store.get(key);
		if (e == null) {
			return Optional.empty();
		}
		if (e.expiresAt().isBefore(Instant.now())) {
			store.remove(key);
			return Optional.empty();
		}
		return Optional.of(e.json());
	}

	@Override
	public void putCachedJson(UserId userId, UUID idempotencyKey, String json) {
		store.put(cacheKey(userId, idempotencyKey), new CacheEntry(json, Instant.now().plus(TTL)));
	}

	private static String cacheKey(UserId userId, UUID idempotencyKey) {
		return userId.value() + ":" + idempotencyKey;
	}

	private record CacheEntry(String json, Instant expiresAt) {
	}
}
