package io.github.alexistrejo11.bank.iam.infrastructure.security;

/**
 * Paths documented for JWT and HTTP security; keep in sync with {@link JwtAuthenticationFilter}.
 */
public final class SecurityWebPaths {

	private SecurityWebPaths() {
	}

	public static boolean shouldSkipJwtAuthentication(String pathWithinApplication) {
		if (pathWithinApplication.startsWith("/actuator") || pathWithinApplication.startsWith("/error")) {
			return true;
		}
		return pathWithinApplication.startsWith("/api/v1/auth/register")
				|| pathWithinApplication.startsWith("/api/v1/auth/login")
				|| pathWithinApplication.startsWith("/api/v1/auth/refresh");
	}
}
