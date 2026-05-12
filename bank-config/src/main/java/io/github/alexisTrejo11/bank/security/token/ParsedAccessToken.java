package io.github.alexisTrejo11.bank.security.token;

import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.time.Instant;
import java.util.Set;

public record ParsedAccessToken(
		UserId userId,
		String email,
		String jti,
		Set<String> roles,
		Set<String> permissions,
		Instant expiresAt) {
}
