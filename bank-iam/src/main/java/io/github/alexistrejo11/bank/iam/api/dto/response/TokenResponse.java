package io.github.alexistrejo11.bank.iam.api.dto.response;

public record TokenResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresInSeconds
) {}
