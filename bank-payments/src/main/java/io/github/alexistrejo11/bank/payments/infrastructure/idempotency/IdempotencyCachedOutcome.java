package io.github.alexistrejo11.bank.payments.infrastructure.idempotency;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.alexistrejo11.bank.payments.api.dto.response.TransferResponse;
import io.github.alexistrejo11.bank.shared.result.Result;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IdempotencyCachedOutcome(
		boolean success,
		TransferResponse transfer,
		String failureCode,
		String failureMessage
) {

	public Result<TransferResponse> toResult() {
		if (success) {
			return Result.success(transfer);
		}
		return Result.failure(failureCode, failureMessage);
	}
}
