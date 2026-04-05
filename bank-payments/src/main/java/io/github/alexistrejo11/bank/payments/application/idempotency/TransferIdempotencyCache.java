package io.github.alexistrejo11.bank.payments.application.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.alexistrejo11.bank.payments.api.dto.response.TransferResponse;
import io.github.alexistrejo11.bank.payments.domain.port.out.TransferIdempotencyPort;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import io.github.alexistrejo11.bank.shared.result.Result;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TransferIdempotencyCache {

	private final TransferIdempotencyPort idempotencyPort;
	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	public TransferIdempotencyCache(TransferIdempotencyPort idempotencyPort) {
		this.idempotencyPort = idempotencyPort;
	}

	public Optional<Result<TransferResponse>> get(UserId userId, UUID idempotencyKey) {
		return idempotencyPort.getCachedJson(userId, idempotencyKey).flatMap(json -> {
			try {
				return Optional.of(objectMapper.readValue(json, IdempotencyCachedOutcome.class).toResult());
			}
			catch (JsonProcessingException ex) {
				return Optional.empty();
			}
		});
	}

	public void put(UserId userId, UUID idempotencyKey, Result<TransferResponse> result) {
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
