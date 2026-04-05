package io.github.alexistrejo11.bank.iam.api.controller;

import io.github.alexistrejo11.bank.iam.api.dto.request.LoginRequest;
import io.github.alexistrejo11.bank.iam.api.dto.request.RefreshRequest;
import io.github.alexistrejo11.bank.iam.api.dto.request.RegisterRequest;
import io.github.alexistrejo11.bank.iam.api.dto.response.MeResponse;
import io.github.alexistrejo11.bank.iam.api.dto.response.TokenResponse;
import io.github.alexistrejo11.bank.iam.application.handler.command.LoginHandler;
import io.github.alexistrejo11.bank.iam.application.handler.command.LogoutHandler;
import io.github.alexistrejo11.bank.iam.application.handler.command.RefreshTokenHandler;
import io.github.alexistrejo11.bank.iam.application.handler.command.RegisterUserHandler;
import io.github.alexistrejo11.bank.iam.infrastructure.security.IamUserPrincipal;
import io.github.alexistrejo11.bank.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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
			LogoutHandler logoutHandler
	) {
		this.registerUserHandler = registerUserHandler;
		this.loginHandler = loginHandler;
		this.refreshTokenHandler = refreshTokenHandler;
		this.logoutHandler = logoutHandler;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<TokenResponse>> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.ok(ApiResponse.success(registerUserHandler.handle(request)));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(ApiResponse.success(loginHandler.handle(request)));
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
		return ResponseEntity.ok(ApiResponse.success(refreshTokenHandler.handle(request)));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return ResponseEntity.status(401).build();
		}
		logoutHandler.handle(authorization.substring(7));
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<MeResponse>> me(@AuthenticationPrincipal IamUserPrincipal principal) {
		return ResponseEntity.ok(ApiResponse.success(new MeResponse(principal.userId().value(), principal.getUsername())));
	}

}
