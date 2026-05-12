package io.github.alexistrejo11.bank.loans.presentation.controller;

import io.github.alexistrejo11.bank.shared.shared_kernel.auth.IamUserPrincipal;
import io.github.alexistrejo11.bank.loans.presentation.dto.request.OriginateLoanRequest;
import io.github.alexistrejo11.bank.loans.presentation.dto.response.LoanDetailResponse;
import io.github.alexistrejo11.bank.loans.presentation.dto.response.PayRepaymentResponse;
import io.github.alexistrejo11.bank.loans.application.handler.command.ApproveLoanHandler;
import io.github.alexistrejo11.bank.loans.application.handler.command.OriginateLoanHandler;
import io.github.alexistrejo11.bank.loans.application.handler.command.PayLoanRepaymentHandler;
import io.github.alexistrejo11.bank.loans.application.handler.query.GetLoanDetailHandler;
import io.github.alexistrejo11.bank.loans.application.command.ApproveLoanCommand;
import io.github.alexistrejo11.bank.loans.application.command.OriginateLoanCommand;
import io.github.alexistrejo11.bank.loans.application.command.PayLoanRepaymentCommand;
import io.github.alexistrejo11.bank.loans.domain.model.LoanAggregate;
import io.github.alexistrejo11.bank.loans.application.query.GetLoanDetailQuery;
import io.github.alexistrejo11.bank.loans.presentation.mapper.LoanApiMapper;
import io.github.alexistrejo11.bank.shared.shared_kernel.api.ApiResponse;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import io.github.alexistrejo11.bank.shared.shared_kernel.openapi.BankApiKeys;
import io.github.alexistrejo11.bank.shared.shared_kernel.openapi.BankApiOperation;
import io.github.alexistrejo11.bank.shared.shared_kernel.ratelimit.RateLimit;
import io.github.alexistrejo11.bank.shared.shared_kernel.ratelimit.RateLimitProfile;
import io.github.alexistrejo11.bank.shared.shared_kernel.ratelimit.RateLimitScope;
import io.github.alexistrejo11.bank.shared.shared_kernel.result.Result;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
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

  private final OriginateLoanHandler originateLoanHandler;
  private final ApproveLoanHandler approveLoanHandler;
  private final GetLoanDetailHandler getLoanDetailHandler;
  private final PayLoanRepaymentHandler payLoanRepaymentHandler;

  public LoanController(
      OriginateLoanHandler originateLoanHandler,
      ApproveLoanHandler approveLoanHandler,
      GetLoanDetailHandler getLoanDetailHandler,
      PayLoanRepaymentHandler payLoanRepaymentHandler) {
    this.originateLoanHandler = originateLoanHandler;
    this.approveLoanHandler = approveLoanHandler;
    this.getLoanDetailHandler = getLoanDetailHandler;
    this.payLoanRepaymentHandler = payLoanRepaymentHandler;
  }

  @PostMapping
  @BankApiOperation(BankApiKeys.LOANS_ORIGINATE)
  @RateLimit(profile = RateLimitProfile.SENSITIVE_OPERATIONS, scope = RateLimitScope.PER_USER)
  public ApiResponse<LoanDetailResponse> originate(
      @AuthenticationPrincipal IamUserPrincipal principal,
      @Valid @RequestBody OriginateLoanRequest request) {
    var userId = principal.userId();
    var command = new OriginateLoanCommand(
        request.checkingAccountId(),
        request.principal(),
        request.currency(),
        request.monthlyInterestRate(),
        request.termMonths());

    LoanAggregate loan = originateLoanHandler.handle(userId, command);

    LoanDetailResponse response = LoanApiMapper.toDetail(loan);
    return ApiResponse.success(response);
  }

  @PostMapping("/{loanId}/approve")
  @RateLimit(profile = RateLimitProfile.SENSITIVE_OPERATIONS, scope = RateLimitScope.PER_USER)
  public ApiResponse<LoanDetailResponse> approve(
      @AuthenticationPrincipal IamUserPrincipal principal,
      @PathVariable UUID loanId) {
    UserId userId = principal.userId();
    var command = new ApproveLoanCommand(loanId);

    LoanAggregate loan = approveLoanHandler.handle(userId, command);

    LoanDetailResponse response = LoanApiMapper.toDetail(loan);
    return ApiResponse.success(response);
  }

  @GetMapping("/{loanId}")
  @BankApiOperation(BankApiKeys.LOANS_GET)
  public ApiResponse<LoanDetailResponse> get(
      @AuthenticationPrincipal IamUserPrincipal principal,
      @PathVariable UUID loanId) {
    var userId = principal.userId();
    var query = new GetLoanDetailQuery(userId, loanId);

    LoanAggregate loan = getLoanDetailHandler.handle(query);

    var response = LoanApiMapper.toDetail(loan);
    return ApiResponse.success(response);
  }

  @PostMapping("/{loanId}/repayments/{repaymentId}/pay")
  @BankApiOperation(BankApiKeys.LOANS_PAY)
  @RateLimit(profile = RateLimitProfile.SENSITIVE_OPERATIONS, scope = RateLimitScope.PER_USER)
  public ResponseEntity<ApiResponse<PayRepaymentResponse>> pay(
      @AuthenticationPrincipal IamUserPrincipal principal,
      @PathVariable UUID loanId,
      @PathVariable UUID repaymentId) {
    var userId = principal.userId();
    var command = new PayLoanRepaymentCommand(loanId, repaymentId);

    Result<LoanAggregate> result = payLoanRepaymentHandler.handle(userId, command);
    if (!result.isSuccess()) {
      var failure = (Result.Failure<?>) result;
      var body = ApiResponse.<PayRepaymentResponse>failure(failure.code(), failure.message());
      return ResponseEntity.unprocessableContent().body(body);
    }

    var response = LoanApiMapper.toPayRepayment(result.getValue(), repaymentId);
    ApiResponse<PayRepaymentResponse> responseBody = ApiResponse.success(response);
    return ResponseEntity.ok(responseBody);
  }
}
