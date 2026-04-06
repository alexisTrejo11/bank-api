package io.github.alexistrejo11.bank.iam.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT access token plus refresh token metadata.")
public record TokenResponse(
		@Schema(description = "RS256 JWT for Authorization: Bearer") String accessToken,
		@Schema(description = "Opaque refresh token for POST /auth/refresh") String refreshToken,
		@Schema(description = "Always Bearer for access token", example = "Bearer") String tokenType,
		@Schema(description = "Access token lifetime in seconds") long expiresInSeconds
) {}
