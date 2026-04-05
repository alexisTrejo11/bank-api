package io.github.alexistrejo11.bank.iam.infrastructure.security;

import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStore {

	void store(String refreshToken, UserId userId, Duration ttl);

	Optional<UserId> findUserId(String refreshToken);

	void remove(String refreshToken);
}
