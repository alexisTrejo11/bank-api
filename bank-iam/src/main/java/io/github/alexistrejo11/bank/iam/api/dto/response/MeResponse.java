package io.github.alexistrejo11.bank.iam.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Authenticated user id and email from the JWT.")
public record MeResponse(
		@Schema(description = "Stable user id") UUID userId,
		@Schema(description = "Email claim") String email
) {}
