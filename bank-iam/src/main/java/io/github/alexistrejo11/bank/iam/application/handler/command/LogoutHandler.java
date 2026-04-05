package io.github.alexistrejo11.bank.iam.application.handler.command;

import io.github.alexistrejo11.bank.iam.infrastructure.security.JwtBlocklistStore;
import io.github.alexistrejo11.bank.iam.infrastructure.security.JwtTokenService;
import io.github.alexistrejo11.bank.iam.infrastructure.security.ParsedAccessToken;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class LogoutHandler {

	private final JwtTokenService jwtTokenService;
	private final JwtBlocklistStore blocklistStore;

	public LogoutHandler(JwtTokenService jwtTokenService, JwtBlocklistStore blocklistStore) {
		this.jwtTokenService = jwtTokenService;
		this.blocklistStore = blocklistStore;
	}

	public void handle(String bearerAccessToken) {
		ParsedAccessToken parsed = jwtTokenService.parseAccessTokenIgnoringBlocklist(bearerAccessToken);
		Duration ttl = Duration.between(Instant.now(), parsed.expiresAt());
		if (ttl.isNegative()) {
			ttl = Duration.ZERO;
		}
		blocklistStore.add(parsed.jti(), ttl);
	}
}
