package io.github.alexistrejo11.bank.payments.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.alexistrejo11.bank.payments.api.dto.response.TransferResponse;
import io.github.alexistrejo11.bank.payments.domain.model.TransferStatus;
import io.github.alexistrejo11.bank.payments.domain.port.out.AccountLedgerInfoPort;
import io.github.alexistrejo11.bank.payments.domain.port.out.AccountLedgerInfoPort.AccountLedgerInfo;
import io.github.alexistrejo11.bank.payments.domain.port.out.TransferIdempotencyPort;
import io.github.alexistrejo11.bank.payments.infrastructure.idempotency.IdempotencyCachedOutcome;
import io.github.alexistrejo11.bank.payments.infrastructure.persistence.entity.TransferEntity;
import io.github.alexistrejo11.bank.payments.infrastructure.persistence.repository.TransferJpaRepository;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferFailedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferReversedEvent;
import io.github.alexistrejo11.bank.shared.exception.InvalidMoneyAmountException;
import io.github.alexistrejo11.bank.shared.exception.ResourceNotFoundException;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.TransferId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import io.github.alexistrejo11.bank.shared.money.Money;
import io.github.alexistrejo11.bank.shared.result.Result;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferApplicationService {

	private final TransferJpaRepository transferRepository;
	private final AccountLedgerInfoPort accountLedgerInfoPort;
	private final TransferIdempotencyPort idempotencyPort;
	private final ApplicationEventPublisher eventPublisher;
	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	public TransferApplicationService(
			TransferJpaRepository transferRepository,
			AccountLedgerInfoPort accountLedgerInfoPort,
			TransferIdempotencyPort idempotencyPort,
			ApplicationEventPublisher eventPublisher
	) {
		this.transferRepository = transferRepository;
		this.accountLedgerInfoPort = accountLedgerInfoPort;
		this.idempotencyPort = idempotencyPort;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public Result<TransferResponse> initiate(
			UserId userId,
			UUID idempotencyKey,
			UUID sourceAccountId,
			UUID targetAccountId,
			BigDecimal amount,
			String currencyRaw
	) {
		Optional<IdempotencyCachedOutcome> cached = loadFromCache(userId, idempotencyKey);
		if (cached.isPresent()) {
			return cached.get().toResult();
		}

		Optional<TransferEntity> existing = transferRepository.findByUserIdAndIdempotencyKey(userId.value(), idempotencyKey);
		if (existing.isPresent()) {
			Result<TransferResponse> r = toResult(existing.get());
			cacheOutcome(userId, idempotencyKey, r);
			return r;
		}

		if (sourceAccountId.equals(targetAccountId)) {
			return persistFailure(userId, idempotencyKey, sourceAccountId, targetAccountId, amount, currencyRaw,
					"SELF_TRANSFER", "Source and target account must differ");
		}

		final Money money;
		try {
			Currency ccy = Currency.getInstance(currencyRaw.trim().toUpperCase());
			money = new Money(amount, ccy);
		}
		catch (InvalidMoneyAmountException e) {
			return failCached(userId, idempotencyKey, "INVALID_AMOUNT", e.getMessage());
		}
		catch (IllegalArgumentException e) {
			return failCached(userId, idempotencyKey, "INVALID_CURRENCY", "Unknown currency code");
		}

		AccountId srcId = AccountId.of(sourceAccountId);
		AccountId tgtId = AccountId.of(targetAccountId);

		AccountLedgerInfo source = accountLedgerInfoPort.find(srcId).orElse(null);
		if (source == null) {
			return failCached(userId, idempotencyKey, "SOURCE_NOT_FOUND", "Source account not found");
		}
		if (!source.ownerId().equals(userId)) {
			return failCached(userId, idempotencyKey, "SOURCE_FORBIDDEN", "You do not own the source account");
		}
		if (!source.active()) {
			return failCached(userId, idempotencyKey, "SOURCE_INACTIVE", "Source account is not active");
		}

		AccountLedgerInfo target = accountLedgerInfoPort.find(tgtId).orElse(null);
		if (target == null) {
			return failCached(userId, idempotencyKey, "TARGET_NOT_FOUND", "Target account not found");
		}
		if (!target.active()) {
			return failCached(userId, idempotencyKey, "TARGET_INACTIVE", "Target account is not active");
		}

		if (!source.currencyCode().equalsIgnoreCase(target.currencyCode())) {
			return persistFailure(userId, idempotencyKey, sourceAccountId, targetAccountId, amount, money.currency().getCurrencyCode(),
					"CURRENCY_MISMATCH", "Source and target accounts use different currencies");
		}
		if (!source.currencyCode().equalsIgnoreCase(money.currency().getCurrencyCode())) {
			return persistFailure(userId, idempotencyKey, sourceAccountId, targetAccountId, amount, money.currency().getCurrencyCode(),
					"CURRENCY_MISMATCH", "Transfer currency does not match account currency");
		}

		if (source.ledgerBalance().compareTo(money.amount()) < 0) {
			return persistFailure(userId, idempotencyKey, sourceAccountId, targetAccountId, amount, money.currency().getCurrencyCode(),
					"INSUFFICIENT_FUNDS", "Source account balance is below the transfer amount");
		}

		Instant now = Instant.now();
		UUID id = UUID.randomUUID();
		TransferEntity row = transferRepository.save(new TransferEntity(
				id,
				userId.value(),
				sourceAccountId,
				targetAccountId,
				money.amount(),
				money.currency().getCurrencyCode(),
				TransferStatus.COMPLETED,
				idempotencyKey,
				null,
				null,
				now,
				now
		));

		eventPublisher.publishEvent(new TransferCompletedEvent(
				TransferId.of(id),
				srcId,
				tgtId,
				money.amount(),
				money.currency().getCurrencyCode()
		));

		Result<TransferResponse> ok = Result.success(toResponse(row));
		cacheOutcome(userId, idempotencyKey, ok);
		return ok;
	}

	@Transactional
	public Result<TransferResponse> reverse(UserId userId, UUID idempotencyKey, UUID originalTransferId) {
		Optional<IdempotencyCachedOutcome> cached = loadFromCache(userId, idempotencyKey);
		if (cached.isPresent()) {
			return cached.get().toResult();
		}

		Optional<TransferEntity> existing = transferRepository.findByUserIdAndIdempotencyKey(userId.value(), idempotencyKey);
		if (existing.isPresent()) {
			Result<TransferResponse> r = toResult(existing.get());
			cacheOutcome(userId, idempotencyKey, r);
			return r;
		}

		TransferEntity original = transferRepository.findById(originalTransferId)
				.orElseThrow(() -> new ResourceNotFoundException("TRANSFER_NOT_FOUND", "Transfer not found"));

		if (!original.getUserId().equals(userId.value())) {
			return failCached(userId, idempotencyKey, "FORBIDDEN", "Only the initiator may reverse this transfer");
		}
		if (original.getStatus() != TransferStatus.COMPLETED) {
			return failCached(userId, idempotencyKey, "INVALID_STATE", "Only completed transfers can be reversed");
		}
		if (original.getReferenceTransferId() != null) {
			return failCached(userId, idempotencyKey, "INVALID_STATE", "Cannot reverse a reversal transfer");
		}

		UUID newSource = original.getTargetAccountId();
		UUID newTarget = original.getSourceAccountId();
		BigDecimal amt = original.getAmount();
		String ccy = original.getCurrency();

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
		TransferEntity reversalRow = transferRepository.save(new TransferEntity(
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

		original.setStatus(TransferStatus.REVERSED);
		original.setUpdatedAt(now);
		transferRepository.save(original);

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

		Result<TransferResponse> ok = Result.success(toResponse(reversalRow));
		cacheOutcome(userId, idempotencyKey, ok);
		return ok;
	}

	private Result<TransferResponse> failCached(UserId userId, UUID idempotencyKey, String code, String message) {
		Result<TransferResponse> r = Result.failure(code, message);
		cacheOutcome(userId, idempotencyKey, r);
		return r;
	}

	private Result<TransferResponse> persistFailure(
			UserId userId,
			UUID idempotencyKey,
			UUID sourceAccountId,
			UUID targetAccountId,
			BigDecimal amount,
			String currency,
			String code,
			String message
	) {
		Instant now = Instant.now();
		UUID id = UUID.randomUUID();
		String ccy = currency != null ? currency : "USD";
		transferRepository.save(new TransferEntity(
				id,
				userId.value(),
				sourceAccountId,
				targetAccountId,
				amount,
				ccy,
				TransferStatus.FAILED,
				idempotencyKey,
				code + "|" + message,
				null,
				now,
				now
		));
		eventPublisher.publishEvent(new TransferFailedEvent(TransferId.of(id), code, message));
		Result<TransferResponse> fail = Result.failure(code, message);
		cacheOutcome(userId, idempotencyKey, fail);
		return fail;
	}

	private Result<TransferResponse> persistFailureReverse(
			UserId userId,
			UUID idempotencyKey,
			TransferEntity original,
			String code,
			String message
	) {
		Instant now = Instant.now();
		UUID id = UUID.randomUUID();
		transferRepository.save(new TransferEntity(
				id,
				userId.value(),
				original.getTargetAccountId(),
				original.getSourceAccountId(),
				original.getAmount(),
				original.getCurrency(),
				TransferStatus.FAILED,
				idempotencyKey,
				code + "|" + message,
				original.getId(),
				now,
				now
		));
		eventPublisher.publishEvent(new TransferFailedEvent(TransferId.of(id), code, message));
		Result<TransferResponse> fail = Result.failure(code, message);
		cacheOutcome(userId, idempotencyKey, fail);
		return fail;
	}

	private Result<TransferResponse> toResult(TransferEntity e) {
		if (e.getStatus() == TransferStatus.FAILED) {
			String fr = e.getFailureReason();
			String code = "TRANSFER_FAILED";
			String msg = fr != null ? fr : "failed";
			if (fr != null && fr.contains("|")) {
				int i = fr.indexOf('|');
				code = fr.substring(0, i);
				msg = fr.substring(i + 1);
			}
			return Result.failure(code, msg);
		}
		return Result.success(toResponse(e));
	}

	private TransferResponse toResponse(TransferEntity e) {
		return new TransferResponse(
				e.getId(),
				e.getStatus().name(),
				e.getSourceAccountId(),
				e.getTargetAccountId(),
				e.getAmount(),
				e.getCurrency(),
				e.getReferenceTransferId(),
				e.getFailureReason(),
				e.getCreatedAt()
		);
	}

	private Optional<IdempotencyCachedOutcome> loadFromCache(UserId userId, UUID idempotencyKey) {
		return idempotencyPort.getCachedJson(userId, idempotencyKey).flatMap(json -> {
			try {
				return Optional.of(objectMapper.readValue(json, IdempotencyCachedOutcome.class));
			}
			catch (JsonProcessingException ex) {
				return Optional.empty();
			}
		});
	}

	private void cacheOutcome(UserId userId, UUID idempotencyKey, Result<TransferResponse> result) {
		try {
			IdempotencyCachedOutcome co;
			if (result.isSuccess()) {
				Result.Success<TransferResponse> s = (Result.Success<TransferResponse>) result;
				co = new IdempotencyCachedOutcome(true, s.value(), null, null);
			}
			else {
				Result.Failure<TransferResponse> f = (Result.Failure<TransferResponse>) result;
				co = new IdempotencyCachedOutcome(false, null, f.code(), f.message());
			}
			idempotencyPort.putCachedJson(userId, idempotencyKey, objectMapper.writeValueAsString(co));
		}
		catch (JsonProcessingException ignored) {
		}
	}
}
