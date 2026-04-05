package io.github.alexistrejo11.bank.iam.infrastructure.security;

import java.time.Duration;

public interface JwtBlocklistStore {

	void add(String jti, Duration ttl);

	boolean isBlocked(String jti);
}
