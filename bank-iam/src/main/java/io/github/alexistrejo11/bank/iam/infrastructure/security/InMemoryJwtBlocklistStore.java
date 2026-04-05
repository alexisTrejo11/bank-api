package io.github.alexistrejo11.bank.iam.infrastructure.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.iam.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryJwtBlocklistStore implements JwtBlocklistStore {

	private final ConcurrentHashMap<String, Instant> entries = new ConcurrentHashMap<>();

	@Override
	public void add(String jti, Duration ttl) {
		entries.put(jti, Instant.now().plus(ttl));
	}

	@Override
	public boolean isBlocked(String jti) {
		Instant exp = entries.get(jti);
		if (exp == null) {
			return false;
		}
		if (exp.isBefore(Instant.now())) {
			entries.remove(jti);
			return false;
		}
		return true;
	}
}
