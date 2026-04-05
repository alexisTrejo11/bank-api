package io.github.alexistrejo11.bank.payments.application.handler.command;

import io.github.alexistrejo11.bank.payments.api.dto.response.TransferResponse;
import io.github.alexistrejo11.bank.payments.api.mapper.TransferApiMapper;
import io.github.alexistrejo11.bank.payments.application.idempotency.TransferIdempotencyCache;
import io.github.alexistrejo11.bank.payments.domain.model.TransferRecord;
import io.github.alexistrejo11.bank.payments.domain.model.TransferStatus;
import io.github.alexistrejo11.bank.payments.domain.port.out.AccountLedgerInfoPort;
import io.github.alexistrejo11.bank.payments.domain.port.out.AccountLedgerInfoPort.AccountLedgerInfo;
import io.github.alexistrejo11.bank.payments.domain.port.out.TransferRepository;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferFailedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferReversedEvent;
import io.github.alexistrejo11.bank.shared.exception.ResourceNotFoundException;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.TransferId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import io.github.alexistrejo11.bank.shared.result.Result;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReverseTransferHandler {

	private final TransferRepository transferRepository;
	private final AccountLedgerInfoPort accountLedgerInfoPort;
	private final TransferIdempotencyCache idempotencyCache;
	private final ApplicationEventPublisher eventPublisher;

	public ReverseTransferHandler(
			TransferRepository transferRepository,
			AccountLedgerInfoPort accountLedgerInfoPort,
			TransferIdempotencyCache idempotencyCache,
			ApplicationEventPublisher eventPublisher
	) {
		this.transferRepository = transferRepository;
		this.accountLedgerInfoPort = accountLedgerInfoPort;
		this.idempotencyCache = idempotencyCache;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public Result<TransferResponse> handle(UserId userId, UUID idempotencyKey, UUID originalTransferId) {
		Optional<Result<TransferResponse>> cached = idempotencyCache.get(userId, idempotencyKey);
		if (cached.isPresent()) {
			return cached.get();
		}

		Optional<TransferRecord> existing = transferRepository.findByUserIdAndIdempotencyKey(userId.value(), idempotencyKey);
		if (existing.isPresent()) {
			Result<TransferResponse> r = TransferApiMapper.toResult(existing.get());
			idempotencyCache.put(userId, idempotencyKey, r);
			return r;
		}

		TransferRecord original = transferRepository.findById(originalTransferId)
				.orElseThrow(() -> new ResourceNotFoundException("TRANSFER_NOT_FOUND", "Transfer not found"));

		if (!original.userId().equals(userId.value())) {
			return failCached(userId, idempotencyKey, "FORBIDDEN", "Only the initiator may reverse this transfer");
		}
		if (original.status() != TransferStatus.COMPLETED) {
			return failCached(userId, idempotencyKey, "INVALID_STATE", "Only completed transfers can be reversed");
		}
		if (original.referenceTransferId() != null) {
			return failCached(userId, idempotencyKey, "INVALID_STATE", "Cannot reverse a reversal transfer");
		}

		UUID newSource = original.targetAccountId();
		UUID newTarget = original.sourceAccountId();
		BigDecimal amt = original.amount();
		String ccy = original.currency();

		AccountLedgerInfo source = accountLedgerInfoPort.find(AccountId.of(newSource)).orElse(null);
		if (source == null || !source.active()) {
			return persistFailureReverse(userId, idempotencyKey, original, "SOURCE_INVALID", "Cannot load source account for reversal");
		}
		AccountLedgerInfo target = accountLedgerInfoPort.find(AccountId.of(newTarget)).orElse(null);
		if (target == null || !target.active()) {
			return persistFailureReverse(userId, idempotencyKey, original, "TARGET_INVALID", "Cannot load target account for reversal");
		}
		if (!source.currencyCode().equalsIgnoreCase(target.currencyCode())) {
			return persistFailureReverse(userId, idempotencyKey, original, "CURRENCY_MISMATCH", "Account currency mismatch");
		}
		if (source.ledgerBalance().compareTo(amt) < 0) {
			return persistFailureReverse(userId, idempotencyKey, original, "INSUFFICIENT_FUNDS", "Insufficient funds to reverse");
		}

		Instant now = Instant.now();
		UUID reversalId = UUID.randomUUID();
		TransferRecord reversalRow = transferRepository.save(new TransferRecord(
				reversalId,
				userId.value(),
				newSource,
				newTarget,
				amt,
				ccy,
				TransferStatus.COMPLETED,
				idempotencyKey,
				null,
				originalTransferId,
				now,
				now
		));

		TransferRecord reversedOriginal = new TransferRecord(
				original.id(),
				original.userId(),
				original.sourceAccountId(),
				original.targetAccountId(),
				original.amount(),
				original.currency(),
				TransferStatus.REVERSED,
				original.idempotencyKey(),
				original.failureReason(),
				original.referenceTransferId(),
				original.createdAt(),
				now
		);
		transferRepository.save(reversedOriginal);

		eventPublisher.publishEvent(new TransferCompletedEvent(
				TransferId.of(reversalId),
				AccountId.of(newSource),
				AccountId.of(newTarget),
				amt,
				ccy
		));
		eventPublisher.publishEvent(new TransferReversedEvent(
				TransferId.of(reversalId),
				TransferId.of(originalTransferId),
				AccountId.of(newSource),
				AccountId.of(newTarget),
				amt,
				ccy
		));

		Result<TransferResponse> ok = Result.success(TransferApiMapper.toResponse(reversalRow));
		idempotencyCache.put(userId, idempotencyKey, ok);
		return ok;
	}

	private Result<TransferResponse> failCached(UserId userId, UUID idempotencyKey, String code, String message) {
		Result<TransferResponse> r = Result.failure(code, message);
		idempotencyCache.put(userId, idempotencyKey, r);
		return r;
	}

	private Result<TransferResponse> persistFailureReverse(
			UserId userId,
			UUID idempotencyKey,
			TransferRecord original,
			String code,
			String message
	) {
		Instant now = Instant.now();
		UUID id = UUID.randomUUID();
		transferRepository.save(new TransferRecord(
				id,
				userId.value(),
				original.targetAccountId(),
				original.sourceAccountId(),
				original.amount(),
				original.currency(),
				TransferStatus.FAILED,
				idempotencyKey,
				code + "|" + message,
				original.id(),
				now,
				now
		));
		eventPublisher.publishEvent(new TransferFailedEvent(TransferId.of(id), code, message));
		Result<TransferResponse> fail = Result.failure(code, message);
		idempotencyCache.put(userId, idempotencyKey, fail);
		return fail;
	}
}
