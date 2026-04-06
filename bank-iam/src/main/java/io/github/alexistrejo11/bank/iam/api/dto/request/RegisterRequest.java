package io.github.alexistrejo11.bank.iam.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Register a new user; assigns the default customer role and returns JWT tokens.")
public record RegisterRequest(
		@Schema(description = "Unique email address.", example = "user@example.com") @NotBlank @Email String email,
		@Schema(description = "Plain-text password (min 8 characters).", example = "Secretpass1!") @NotBlank @Size(min = 8, max = 128) String password
) {}
