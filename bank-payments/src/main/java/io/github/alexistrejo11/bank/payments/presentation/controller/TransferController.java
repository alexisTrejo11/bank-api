package io.github.alexistrejo11.bank.payments.presentation.controller;

import io.github.alexistrejo11.bank.shared.shared_kernel.auth.IamUserPrincipal;
import io.github.alexistrejo11.bank.payments.application.command.InitiateTransferCommand;
import io.github.alexistrejo11.bank.payments.application.command.ReverseTransferCommand;
import io.github.alexistrejo11.bank.payments.application.handler.InitiateTransferHandler;
import io.github.alexistrejo11.bank.payments.application.handler.ReverseTransferHandler;
import io.github.alexistrejo11.bank.payments.presentation.dto.request.TransferFundsRequest;
import io.github.alexistrejo11.bank.payments.presentation.dto.response.TransferResponse;
import io.github.alexistrejo11.bank.shared.shared_kernel.api.ApiResponse;
import io.github.alexistrejo11.bank.shared.shared_kernel.openapi.BankApiKeys;
import io.github.alexistrejo11.bank.shared.shared_kernel.openapi.BankApiOperation;
import io.github.alexistrejo11.bank.shared.shared_kernel.ratelimit.RateLimit;
import io.github.alexistrejo11.bank.shared.shared_kernel.ratelimit.RateLimitProfile;
import io.github.alexistrejo11.bank.shared.shared_kernel.ratelimit.RateLimitScope;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import io.github.alexistrejo11.bank.shared.shared_kernel.result.Result;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
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

	public TransferController(InitiateTransferHandler initiateTransferHandler,
			ReverseTransferHandler reverseTransferHandler) {
		this.initiateTransferHandler = initiateTransferHandler;
		this.reverseTransferHandler = reverseTransferHandler;
	}

	@PostMapping("/transfers")
	@BankApiOperation(BankApiKeys.PAYMENTS_TRANSFER)
	public ResponseEntity<ApiResponse<?>> transfer(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@RequestHeader("Idempotency-Key") UUID idempotencyKey,
			@Valid @RequestBody TransferFundsRequest request) {
		UserId userId = principal.userId();
		var command = new InitiateTransferCommand(
				userId,
				idempotencyKey,
				request.sourceAccountId(),
				request.targetAccountId(),
				request.amount(),
				request.currency());

		Result<TransferResponse> result = initiateTransferHandler.handle(command);
		return toResponse(result);
	}

	@PostMapping("/transfers/{transferId}/reverse")
	@BankApiOperation(BankApiKeys.PAYMENTS_REVERSE)
	public ResponseEntity<ApiResponse<?>> reverse(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@RequestHeader("Idempotency-Key") UUID idempotencyKey,
			@PathVariable UUID transferId) {

		var command = new ReverseTransferCommand(principal.userId(), idempotencyKey, transferId);
		Result<TransferResponse> result = reverseTransferHandler.handle(command);
		return toResponse(result);
	}

	private static ResponseEntity<ApiResponse<?>> toResponse(Result<TransferResponse> result) {
		if (!result.isSuccess()) {
			var errorResponseBody = ApiResponse.failure(result.getErrorCode(), result.getErrorMessage());
			return ResponseEntity.unprocessableContent().body(errorResponseBody);
		}

		var successResponseBody = ApiResponse.success(result.getValue());
		return ResponseEntity.ok(successResponseBody);
	}
}
