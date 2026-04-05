package io.github.alexistrejo11.bank.iam.application.handler.command;

import io.github.alexistrejo11.bank.iam.api.dto.request.LoginRequest;
import io.github.alexistrejo11.bank.iam.api.dto.response.TokenResponse;
import io.github.alexistrejo11.bank.iam.application.AuthTokenService;
import io.github.alexistrejo11.bank.iam.domain.model.User;
import io.github.alexistrejo11.bank.iam.domain.model.UserStatus;
import io.github.alexistrejo11.bank.iam.domain.port.out.UserRepository;
import io.github.alexistrejo11.bank.iam.exception.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoginHandler {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthTokenService authTokenService;

	public LoginHandler(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AuthTokenService authTokenService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authTokenService = authTokenService;
	}

	@Transactional(readOnly = true)
	public TokenResponse handle(LoginRequest request) {
		User user = userRepository.findByEmail(request.email()).orElseThrow(InvalidCredentialsException::new);
		if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
			throw new InvalidCredentialsException();
		}
		if (user.status() != UserStatus.ACTIVE) {
			throw new InvalidCredentialsException();
		}
		return authTokenService.issueForUser(user);
	}
}
