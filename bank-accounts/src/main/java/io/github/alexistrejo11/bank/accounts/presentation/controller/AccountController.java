package io.github.alexistrejo11.bank.accounts.presentation.controller;

import io.github.alexistrejo11.bank.accounts.application.handler.command.OpenAccountHandler;
import io.github.alexistrejo11.bank.accounts.application.handler.query.GetAccountBalanceHandler;
import io.github.alexistrejo11.bank.accounts.application.handler.query.GetAccountLedgerHandler;
import io.github.alexistrejo11.bank.accounts.application.command.OpenAccountCommand;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountBalance;
import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntry;
import io.github.alexistrejo11.bank.accounts.domain.model.OpenedAccount;
import io.github.alexistrejo11.bank.accounts.application.query.GetAccountBalanceQuery;
import io.github.alexistrejo11.bank.accounts.application.query.GetAccountLedgerQuery;
import io.github.alexistrejo11.bank.accounts.presentation.dto.request.OpenAccountRequest;
import io.github.alexistrejo11.bank.accounts.presentation.dto.response.BalanceResponse;
import io.github.alexistrejo11.bank.accounts.presentation.dto.response.LedgerPageResponse;
import io.github.alexistrejo11.bank.accounts.presentation.dto.response.OpenAccountResponse;
import io.github.alexistrejo11.bank.accounts.presentation.mapper.AccountApiMapper;
import io.github.alexistrejo11.bank.shared.shared_kernel.auth.IamUserPrincipal;
import io.github.alexistrejo11.bank.shared.shared_kernel.api.ApiResponse;
import io.github.alexistrejo11.bank.shared.shared_kernel.openapi.BankApiKeys;
import io.github.alexistrejo11.bank.shared.shared_kernel.openapi.BankApiOperation;
import io.github.alexistrejo11.bank.shared.shared_kernel.page.PageResult;
import jakarta.validation.Valid;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
			GetAccountLedgerHandler getAccountLedgerHandler) {
		this.openAccountHandler = openAccountHandler;
		this.getAccountBalanceHandler = getAccountBalanceHandler;
		this.getAccountLedgerHandler = getAccountLedgerHandler;
	}

	@PostMapping
	@BankApiOperation(BankApiKeys.ACCOUNTS_OPEN)
	public ApiResponse<OpenAccountResponse> open(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@Valid @RequestBody OpenAccountRequest request) {
		var command = new OpenAccountCommand(principal.userId(), request.type(), request.currency());

		OpenedAccount opened = openAccountHandler.handle(command);

		var response = AccountApiMapper.toOpenAccountResponse(opened);
		return ApiResponse.success(response);
	}

	@GetMapping("/{accountId}/balance")
	public ApiResponse<BalanceResponse> balance(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@PathVariable UUID accountId) {
		var query = new GetAccountBalanceQuery(principal.userId(), accountId);

		AccountBalance balance = getAccountBalanceHandler.handle(query);

		BalanceResponse response = AccountApiMapper.toBalanceResponse(balance);
		return ApiResponse.success(response);
	}

	@GetMapping("/{accountId}/ledger")
	@BankApiOperation(BankApiKeys.ACCOUNTS_LEDGER)
	public ApiResponse<LedgerPageResponse> ledger(
			@AuthenticationPrincipal IamUserPrincipal principal,
			@PathVariable UUID accountId,
			@PageableDefault(size = 20) Pageable pageable) {
		var query = new GetAccountLedgerQuery(
				principal.userId(),
				accountId,
				pageable.getPageNumber(),
				pageable.getPageSize());

		PageResult<LedgerEntry> ledgerPage = getAccountLedgerHandler.handle(query);

		var ledgerPageResponse = AccountApiMapper.toLedgerPageResponse(ledgerPage);
		return ApiResponse.success(ledgerPageResponse);
	}
}
