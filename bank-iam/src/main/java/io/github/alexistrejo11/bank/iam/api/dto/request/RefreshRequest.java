package io.github.alexistrejo11.bank.iam.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Rotate access token using a valid refresh token.")
public record RefreshRequest(
		@Schema(description = "Opaque refresh token from login/register/refresh.") @NotBlank String refreshToken
) {}
