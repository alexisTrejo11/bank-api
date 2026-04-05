package io.github.alexistrejo11.bank.payments.api.mapper;

import io.github.alexistrejo11.bank.payments.api.dto.response.TransferResponse;
import io.github.alexistrejo11.bank.payments.domain.model.TransferRecord;
import io.github.alexistrejo11.bank.payments.domain.model.TransferStatus;
import io.github.alexistrejo11.bank.shared.result.Result;

public final class TransferApiMapper {

	private TransferApiMapper() {
	}

	public static TransferResponse toResponse(TransferRecord e) {
		return new TransferResponse(
				e.id(),
				e.status().name(),
				e.sourceAccountId(),
				e.targetAccountId(),
				e.amount(),
				e.currency(),
				e.referenceTransferId(),
				e.failureReason(),
				e.createdAt()
		);
	}

	public static Result<TransferResponse> toResult(TransferRecord e) {
		if (e.status() == TransferStatus.FAILED) {
			String fr = e.failureReason();
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
}
