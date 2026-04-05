package io.github.alexistrejo11.bank.iam.domain.model;

import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.util.Set;

/**
 * Domain view of a user for application use cases (includes password hash for credential checks only).
 */
public record User(
		UserId id,
		String email,
		String passwordHash,
		UserStatus status,
		Set<String> roleNames,
		Set<String> permissionNames
) {}
