package io.github.alexistrejo11.bank.loans.api.controller;

import io.github.alexistrejo11.bank.loans.api.dto.request.OriginateLoanRequest;
import io.github.alexistrejo11.bank.loans.api.dto.response.LoanDetailResponse;
import io.github.alexistrejo11.bank.loans.api.dto.response.PayRepaymentResponse;
import io.github.alexistrejo11.bank.loans.application.LoanApplicationService;
import io.github.alexistrejo11.bank.iam.infrastructure.security.IamUserPrincipal;
import io.github.alexistrejo11.bank.shared.api.ApiResponse;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import io.github.alexistrejo11.bank.shared.result.Result;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

	private final LoanApplicationService loanApplicationService;

	public LoanController(LoanApplicationService loanApplicationService) {
		this.loanApplicationService = loanApplicationService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('loans:write')")
	public ResponseEntity<ApiResponse<LoanDetailResponse>> originate(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@Valid @RequestBody OriginateLoanRequest request
	) {
		UserId userId = principal.userId();
		return ResponseEntity.ok(ApiResponse.success(loanApplicationService.originate(userId, request)));
	}

	@PostMapping("/{loanId}/approve")
	@PreAuthorize("hasAuthority('loans:write')")
	public ResponseEntity<ApiResponse<LoanDetailResponse>> approve(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@PathVariable UUID loanId
	) {
		return ResponseEntity.ok(ApiResponse.success(loanApplicationService.approve(principal.userId(), loanId)));
	}

	@GetMapping("/{loanId}")
	@PreAuthorize("hasAuthority('loans:read')")
	public ResponseEntity<ApiResponse<LoanDetailResponse>> get(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@PathVariable UUID loanId
	) {
		return ResponseEntity.ok(ApiResponse.success(loanApplicationService.get(principal.userId(), loanId)));
	}

	@PostMapping("/{loanId}/repayments/{repaymentId}/pay")
	@PreAuthorize("hasAuthority('loans:write')")
	public ResponseEntity<ApiResponse<PayRepaymentResponse>> pay(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@PathVariable UUID loanId,
			@PathVariable UUID repaymentId
	) {
		Result<PayRepaymentResponse> result = loanApplicationService.payInstallment(principal.userId(), loanId, repaymentId);
		if (!result.isSuccess()) {
			Result.Failure<?> f = (Result.Failure<?>) result;
			return ResponseEntity.unprocessableEntity().body(ApiResponse.failure(f.code(), f.message()));
		}
		Result.Success<PayRepaymentResponse> s = (Result.Success<PayRepaymentResponse>) result;
		return ResponseEntity.ok(ApiResponse.success(s.value()));
	}
}
