package io.github.alexistrejo11.bank.iam.presentation.controller;

import io.github.alexistrejo11.bank.iam.application.handler.command.LoginHandler;
import io.github.alexistrejo11.bank.iam.application.handler.command.LogoutHandler;
import io.github.alexistrejo11.bank.iam.application.handler.command.RefreshTokenHandler;
import io.github.alexistrejo11.bank.iam.application.handler.command.RegisterUserHandler;
import io.github.alexistrejo11.bank.shared.shared_kernel.auth.IamUserPrincipal;
import io.github.alexistrejo11.bank.iam.presentation.dto.request.LoginRequest;
import io.github.alexistrejo11.bank.iam.presentation.dto.request.RefreshRequest;
import io.github.alexistrejo11.bank.iam.presentation.dto.request.RegisterRequest;
import io.github.alexistrejo11.bank.iam.presentation.dto.response.MeResponse;
import io.github.alexistrejo11.bank.iam.presentation.dto.response.TokenResponse;
import io.github.alexistrejo11.bank.shared.shared_kernel.api.ApiResponse;
import io.github.alexistrejo11.bank.shared.shared_kernel.openapi.BankApiKeys;
import io.github.alexistrejo11.bank.shared.shared_kernel.openapi.BankApiOperation;
import io.github.alexistrejo11.bank.shared.shared_kernel.ratelimit.RateLimit;
import io.github.alexistrejo11.bank.shared.shared_kernel.ratelimit.RateLimitProfile;
import io.github.alexistrejo11.bank.shared.shared_kernel.ratelimit.RateLimitScope;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final RegisterUserHandler registerUserHandler;
	private final LoginHandler loginHandler;
	private final RefreshTokenHandler refreshTokenHandler;
	private final LogoutHandler logoutHandler;

	public AuthController(
			RegisterUserHandler registerUserHandler,
			LoginHandler loginHandler,
			RefreshTokenHandler refreshTokenHandler,
			LogoutHandler logoutHandler) {
		this.registerUserHandler = registerUserHandler;
		this.loginHandler = loginHandler;
		this.refreshTokenHandler = refreshTokenHandler;
		this.logoutHandler = logoutHandler;
	}

	@PostMapping("/register")
	@BankApiOperation(BankApiKeys.AUTH_REGISTER)
	@ResponseStatus(HttpStatus.CREATED)
	@RateLimit(profile = RateLimitProfile.STRICT, scope = RateLimitScope.PER_IP)
	public ApiResponse<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
		TokenResponse tokenResponse = registerUserHandler.handle(request);
		return ApiResponse.success(tokenResponse);
	}

	@PostMapping("/login")
	@BankApiOperation(BankApiKeys.AUTH_LOGIN)
	@RateLimit(profile = RateLimitProfile.STRICT, scope = RateLimitScope.PER_IP)
	public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		TokenResponse tokenResponse = loginHandler.handle(request);
		return ApiResponse.success(tokenResponse);
	}

	@PostMapping("/refresh")
	@BankApiOperation(BankApiKeys.AUTH_REFRESH)
	@RateLimit(profile = RateLimitProfile.STRICT, scope = RateLimitScope.PER_IP)
	public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
		TokenResponse tokenResponse = refreshTokenHandler.handle(request);
		return ApiResponse.success(tokenResponse);
	}

	@PostMapping("/logout")
	@BankApiOperation(BankApiKeys.AUTH_LOGOUT)
	@RateLimit(profile = RateLimitProfile.STANDARD, scope = RateLimitScope.PER_USER)
	public ResponseEntity<Void> logout(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return ResponseEntity.status(401).build();
		}

		logoutHandler.handle(authorization.substring(7));

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	@BankApiOperation(BankApiKeys.AUTH_ME)
	@RateLimit(profile = RateLimitProfile.STANDARD, scope = RateLimitScope.PER_USER)
	public ApiResponse<MeResponse> me(@AuthenticationPrincipal IamUserPrincipal principal) {
		MeResponse meResponse = new MeResponse(principal.userId().value(), principal.getUsername());
		return ApiResponse.success(meResponse);
	}

}
