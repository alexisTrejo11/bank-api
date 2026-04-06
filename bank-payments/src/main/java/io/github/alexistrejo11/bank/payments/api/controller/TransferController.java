package io.github.alexistrejo11.bank.payments.api.controller;

import io.github.alexistrejo11.bank.payments.api.dto.request.TransferFundsRequest;
import io.github.alexistrejo11.bank.payments.api.dto.response.TransferResponse;
import io.github.alexistrejo11.bank.payments.application.handler.command.InitiateTransferHandler;
import io.github.alexistrejo11.bank.payments.application.handler.command.ReverseTransferHandler;
import io.github.alexistrejo11.bank.iam.infrastructure.security.IamUserPrincipal;
import io.github.alexistrejo11.bank.shared.api.ApiResponse;
import io.github.alexistrejo11.bank.shared.ratelimit.RateLimit;
import io.github.alexistrejo11.bank.shared.ratelimit.RateLimitProfile;
import io.github.alexistrejo11.bank.shared.ratelimit.RateLimitScope;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import io.github.alexistrejo11.bank.shared.result.Result;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RateLimit(profile = RateLimitProfile.STRICT, scope = RateLimitScope.PER_USER)
public class TransferController {

	private final InitiateTransferHandler initiateTransferHandler;
	private final ReverseTransferHandler reverseTransferHandler;

	public TransferController(InitiateTransferHandler initiateTransferHandler, ReverseTransferHandler reverseTransferHandler) {
		this.initiateTransferHandler = initiateTransferHandler;
		this.reverseTransferHandler = reverseTransferHandler;
	}

	@PostMapping("/transfers")
	@PreAuthorize("hasAuthority('payments:write')")
	public ResponseEntity<ApiResponse<TransferResponse>> transfer(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@RequestHeader("Idempotency-Key") UUID idempotencyKey,
			@Valid @RequestBody TransferFundsRequest request
	) {
		UserId userId = principal.userId();
		Result<TransferResponse> result = initiateTransferHandler.handle(
				userId,
				idempotencyKey,
				request.sourceAccountId(),
				request.targetAccountId(),
				request.amount(),
				request.currency()
		);
		return toResponse(result);
	}

	@PostMapping("/transfers/{transferId}/reverse")
	@PreAuthorize("hasAuthority('payments:write')")
	public ResponseEntity<ApiResponse<TransferResponse>> reverse(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@RequestHeader("Idempotency-Key") UUID idempotencyKey,
			@PathVariable UUID transferId
	) {
		Result<TransferResponse> result = reverseTransferHandler.handle(principal.userId(), idempotencyKey, transferId);
		return toResponse(result);
	}

	private static ResponseEntity<ApiResponse<TransferResponse>> toResponse(Result<TransferResponse> result) {
		if (!result.isSuccess()) {
			Result.Failure<?> f = (Result.Failure<?>) result;
			return ResponseEntity.unprocessableEntity().body(ApiResponse.failure(f.code(), f.message()));
		}
		Result.Success<TransferResponse> s = (Result.Success<TransferResponse>) result;
		return ResponseEntity.ok(ApiResponse.success(s.value()));
	}
}
