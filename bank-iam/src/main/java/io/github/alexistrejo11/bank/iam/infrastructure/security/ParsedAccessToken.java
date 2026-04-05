package io.github.alexistrejo11.bank.iam.infrastructure.security;

import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.time.Instant;
import java.util.Set;

public record ParsedAccessToken(
		UserId userId,
		String email,
		String jti,
		Set<String> roles,
		Set<String> permissions,
		Instant expiresAt
) {}
