package io.github.alexistrejo11.bank.accounts.api.controller;

import io.github.alexistrejo11.bank.accounts.api.dto.request.OpenAccountRequest;
import io.github.alexistrejo11.bank.accounts.api.dto.response.BalanceResponse;
import io.github.alexistrejo11.bank.accounts.api.dto.response.LedgerPageResponse;
import io.github.alexistrejo11.bank.accounts.api.dto.response.OpenAccountResponse;
import io.github.alexistrejo11.bank.accounts.application.AccountApplicationService;
import io.github.alexistrejo11.bank.iam.infrastructure.security.IamUserPrincipal;
import io.github.alexistrejo11.bank.shared.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/accounts")
public class AccountController {

	private final AccountApplicationService accountApplicationService;

	public AccountController(AccountApplicationService accountApplicationService) {
		this.accountApplicationService = accountApplicationService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('accounts:write')")
	public ResponseEntity<ApiResponse<OpenAccountResponse>> open(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@Valid @RequestBody OpenAccountRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success(accountApplicationService.open(principal.userId(), request)));
	}

	@GetMapping("/{accountId}/balance")
	@PreAuthorize("hasAuthority('accounts:read')")
	public ResponseEntity<ApiResponse<BalanceResponse>> balance(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@PathVariable UUID accountId
	) {
		return ResponseEntity.ok(ApiResponse.success(accountApplicationService.getBalance(principal.userId(), accountId)));
	}

	@GetMapping("/{accountId}/ledger")
	@PreAuthorize("hasAuthority('accounts:read')")
	public ResponseEntity<ApiResponse<LedgerPageResponse>> ledger(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@PathVariable UUID accountId,
			@PageableDefault(size = 20) Pageable pageable
	) {
		return ResponseEntity.ok(ApiResponse.success(accountApplicationService.getLedger(principal.userId(), accountId, pageable)));
	}
}
