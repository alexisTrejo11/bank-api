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
import io.github.alexistrejo11.bank.shared.exception.InvalidMoneyAmountException;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitiateTransferHandler {

	private final TransferRepository transferRepository;
	private final AccountLedgerInfoPort accountLedgerInfoPort;
	private final TransferIdempotencyCache idempotencyCache;
	private final ApplicationEventPublisher eventPublisher;

	public InitiateTransferHandler(
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
	public Result<TransferResponse> handle(
			UserId userId,
			UUID idempotencyKey,
			UUID sourceAccountId,
			UUID targetAccountId,
			BigDecimal amount,
			String currencyRaw
	) {
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
		TransferRecord row = transferRepository.save(new TransferRecord(
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

		Result<TransferResponse> ok = Result.success(TransferApiMapper.toResponse(row));
		idempotencyCache.put(userId, idempotencyKey, ok);
		return ok;
	}

	private Result<TransferResponse> failCached(UserId userId, UUID idempotencyKey, String code, String message) {
		Result<TransferResponse> r = Result.failure(code, message);
		idempotencyCache.put(userId, idempotencyKey, r);
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
		transferRepository.save(new TransferRecord(
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
		idempotencyCache.put(userId, idempotencyKey, fail);
		return fail;
	}
}
