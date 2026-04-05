package io.github.alexistrejo11.bank.payments.domain.port.out;

import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.util.Optional;
import java.util.UUID;

/** Caches transfer API JSON responses per (user, idempotency key); TTL 24h. */
public interface TransferIdempotencyPort {

	Optional<String> getCachedJson(UserId userId, UUID idempotencyKey);

	void putCachedJson(UserId userId, UUID idempotencyKey, String json);
}
