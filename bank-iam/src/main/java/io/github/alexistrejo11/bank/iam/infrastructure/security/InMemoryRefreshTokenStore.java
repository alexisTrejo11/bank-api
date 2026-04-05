package io.github.alexistrejo11.bank.iam.infrastructure.security;

import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.iam.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

	private record Entry(UserId userId, Instant expiresAt) {}

	private final ConcurrentHashMap<String, Entry> byTokenHash = new ConcurrentHashMap<>();

	@Override
	public void store(String refreshToken, UserId userId, Duration ttl) {
		String key = Sha256.hex(refreshToken);
		byTokenHash.put(key, new Entry(userId, Instant.now().plus(ttl)));
	}

	@Override
	public Optional<UserId> findUserId(String refreshToken) {
		String key = Sha256.hex(refreshToken);
		Entry e = byTokenHash.get(key);
		if (e == null) {
			return Optional.empty();
		}
		if (e.expiresAt.isBefore(Instant.now())) {
			byTokenHash.remove(key);
			return Optional.empty();
		}
		return Optional.of(e.userId);
	}

	@Override
	public void remove(String refreshToken) {
		byTokenHash.remove(Sha256.hex(refreshToken));
	}
}
