package io.github.alexistrejo11.bank.iam.application.handler.command;

import io.github.alexistrejo11.bank.iam.api.dto.request.RefreshRequest;
import io.github.alexistrejo11.bank.iam.api.dto.response.TokenResponse;
import io.github.alexistrejo11.bank.iam.application.AuthTokenService;
import io.github.alexistrejo11.bank.iam.domain.model.User;
import io.github.alexistrejo11.bank.iam.domain.port.out.UserRepository;
import io.github.alexistrejo11.bank.iam.exception.InvalidCredentialsException;
import io.github.alexistrejo11.bank.iam.infrastructure.security.RefreshTokenStore;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RefreshTokenHandler {

	private final RefreshTokenStore refreshTokenStore;
	private final UserRepository userRepository;
	private final AuthTokenService authTokenService;

	public RefreshTokenHandler(
			RefreshTokenStore refreshTokenStore,
			UserRepository userRepository,
			AuthTokenService authTokenService
	) {
		this.refreshTokenStore = refreshTokenStore;
		this.userRepository = userRepository;
		this.authTokenService = authTokenService;
	}

	@Transactional
	public TokenResponse handle(RefreshRequest request) {
		UserId userId = refreshTokenStore.findUserId(request.refreshToken()).orElseThrow(InvalidCredentialsException::new);
		User user = userRepository.findById(userId).orElseThrow(InvalidCredentialsException::new);
		authTokenService.revokeRefreshToken(request.refreshToken());
		return authTokenService.issueForUser(user);
	}
}
