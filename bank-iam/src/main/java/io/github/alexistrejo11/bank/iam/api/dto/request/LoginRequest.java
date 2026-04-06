package io.github.alexistrejo11.bank.iam.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credentials for password grant; returns access and refresh JWTs.")
public record LoginRequest(
		@Schema(example = "user@example.com") @NotBlank @Email String email,
		@Schema(description = "Plain-text password.") @NotBlank String password
) {}
