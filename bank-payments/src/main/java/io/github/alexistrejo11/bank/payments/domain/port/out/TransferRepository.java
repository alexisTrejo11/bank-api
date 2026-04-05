package io.github.alexistrejo11.bank.payments.domain.port.out;

import io.github.alexistrejo11.bank.payments.domain.model.TransferRecord;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository {

	Optional<TransferRecord> findByUserIdAndIdempotencyKey(UUID userId, UUID idempotencyKey);

	Optional<TransferRecord> findById(UUID id);

	TransferRecord save(TransferRecord transfer);
}
