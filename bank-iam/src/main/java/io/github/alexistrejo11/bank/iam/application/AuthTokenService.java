package io.github.alexistrejo11.bank.iam.application;

import io.github.alexistrejo11.bank.iam.api.dto.response.TokenResponse;
import io.github.alexistrejo11.bank.iam.domain.model.User;
import io.github.alexistrejo11.bank.iam.infrastructure.security.JwtTokenService;
import io.github.alexistrejo11.bank.iam.infrastructure.security.RefreshTokenStore;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

	private static final Duration ACCESS_TTL = Duration.ofMinutes(15);
	private static final Duration REFRESH_TTL = Duration.ofDays(7);
	private static final SecureRandom RANDOM = new SecureRandom();

	private final JwtTokenService jwtTokenService;
	private final RefreshTokenStore refreshTokenStore;

	public AuthTokenService(JwtTokenService jwtTokenService, RefreshTokenStore refreshTokenStore) {
		this.jwtTokenService = jwtTokenService;
		this.refreshTokenStore = refreshTokenStore;
	}

	public TokenResponse issueForUser(User user) {
		String access = jwtTokenService.createAccessToken(
				user.id(),
				user.email(),
				user.roleNames(),
				user.permissionNames(),
				ACCESS_TTL
		);
		String refresh = newRefreshToken();
		refreshTokenStore.store(refresh, user.id(), REFRESH_TTL);
		return new TokenResponse(access, refresh, "Bearer", ACCESS_TTL.toSeconds());
	}

	public void revokeRefreshToken(String refreshToken) {
		refreshTokenStore.remove(refreshToken);
	}

	private static String newRefreshToken() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}
}
