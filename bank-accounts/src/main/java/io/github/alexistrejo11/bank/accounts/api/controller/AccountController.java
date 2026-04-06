package io.github.alexistrejo11.bank.accounts.api.controller;

import io.github.alexistrejo11.bank.accounts.api.dto.request.OpenAccountRequest;
import io.github.alexistrejo11.bank.accounts.api.dto.response.BalanceResponse;
import io.github.alexistrejo11.bank.accounts.api.dto.response.LedgerPageResponse;
import io.github.alexistrejo11.bank.accounts.api.dto.response.OpenAccountResponse;
import io.github.alexistrejo11.bank.accounts.api.mapper.AccountApiMapper;
import io.github.alexistrejo11.bank.accounts.application.handler.command.OpenAccountHandler;
import io.github.alexistrejo11.bank.accounts.application.handler.query.GetAccountBalanceHandler;
import io.github.alexistrejo11.bank.accounts.application.handler.query.GetAccountLedgerHandler;
import io.github.alexistrejo11.bank.accounts.domain.command.OpenAccountCommand;
import io.github.alexistrejo11.bank.accounts.domain.query.GetAccountBalanceQuery;
import io.github.alexistrejo11.bank.accounts.domain.query.GetAccountLedgerQuery;
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

	private final OpenAccountHandler openAccountHandler;
	private final GetAccountBalanceHandler getAccountBalanceHandler;
	private final GetAccountLedgerHandler getAccountLedgerHandler;

	public AccountController(
			OpenAccountHandler openAccountHandler,
			GetAccountBalanceHandler getAccountBalanceHandler,
			GetAccountLedgerHandler getAccountLedgerHandler
	) {
		this.openAccountHandler = openAccountHandler;
		this.getAccountBalanceHandler = getAccountBalanceHandler;
		this.getAccountLedgerHandler = getAccountLedgerHandler;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('accounts:write')")
	public ResponseEntity<ApiResponse<OpenAccountResponse>> open(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@Valid @RequestBody OpenAccountRequest request
	) {
		var opened = openAccountHandler.handle(new OpenAccountCommand(principal.userId(), request.type(), request.currency()));
		return ResponseEntity.ok(ApiResponse.success(AccountApiMapper.toOpenResponse(opened)));
	}

	@GetMapping("/{accountId}/balance")
	@PreAuthorize("hasAuthority('accounts:read')")
	public ResponseEntity<ApiResponse<BalanceResponse>> balance(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@PathVariable UUID accountId
	) {
		var balance = getAccountBalanceHandler.handle(new GetAccountBalanceQuery(principal.userId(), accountId));
		return ResponseEntity.ok(ApiResponse.success(AccountApiMapper.toBalanceResponse(balance)));
	}

	@GetMapping("/{accountId}/ledger")
	@PreAuthorize("hasAuthority('accounts:read')")
	public ResponseEntity<ApiResponse<LedgerPageResponse>> ledger(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@PathVariable UUID accountId,
			@PageableDefault(size = 20) Pageable pageable
	) {
		var page = getAccountLedgerHandler.handle(new GetAccountLedgerQuery(
				principal.userId(),
				accountId,
				pageable.getPageNumber(),
				pageable.getPageSize()
		));
		return ResponseEntity.ok(ApiResponse.success(AccountApiMapper.toLedgerPageResponse(page)));
	}
}
