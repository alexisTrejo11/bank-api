package io.github.alexistrejo11.bank.iam.api.dto.response;

import java.util.UUID;

public record MeResponse(UUID userId, String email) {}
