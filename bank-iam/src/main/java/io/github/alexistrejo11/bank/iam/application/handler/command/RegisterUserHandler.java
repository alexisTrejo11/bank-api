package io.github.alexistrejo11.bank.iam.application.handler.command;

import io.github.alexistrejo11.bank.iam.api.dto.request.RegisterRequest;
import io.github.alexistrejo11.bank.iam.api.dto.response.TokenResponse;
import io.github.alexistrejo11.bank.iam.application.AuthTokenService;
import io.github.alexistrejo11.bank.iam.domain.model.User;
import io.github.alexistrejo11.bank.iam.domain.model.UserStatus;
import io.github.alexistrejo11.bank.iam.domain.port.out.UserRepository;
import io.github.alexistrejo11.bank.iam.exception.EmailAlreadyRegisteredException;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegisterUserHandler {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthTokenService authTokenService;

	public RegisterUserHandler(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AuthTokenService authTokenService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authTokenService = authTokenService;
	}

	@Transactional
	public TokenResponse handle(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new EmailAlreadyRegisteredException(request.email());
		}
		UserId id = UserId.random();
		String hash = passwordEncoder.encode(request.password());
		User user = new User(
				id,
				request.email(),
				hash,
				UserStatus.ACTIVE,
				Set.of("USER"),
				Set.of()
		);
		userRepository.save(user);
		User loaded = userRepository.findByEmail(request.email()).orElseThrow();
		return authTokenService.issueForUser(loaded);
	}
}
