package io.github.alexisTrejo11.bank.security.token.jwt;

import java.time.Duration;

public interface JwtBlocklistStore {

	void add(String jti, Duration ttl);

	boolean isBlocked(String jti);
}
