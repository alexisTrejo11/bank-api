package io.github.alexisTrejo11.bank.security.token;

import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStore {

	void store(String refreshToken, UserId userId, Duration ttl);

	Optional<UserId> findUserId(String refreshToken);

	void remove(String refreshToken);
}
