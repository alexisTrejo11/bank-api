package io.github.alexistrejo11.bank.loans.application.handler.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.alexistrejo11.bank.loans.domain.model.LoanAggregate;
import io.github.alexistrejo11.bank.loans.domain.model.LoanRepaymentLine;
import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import io.github.alexistrejo11.bank.loans.domain.command.PayLoanRepaymentCommand;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanLedgerOperationsPort;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanRepository;
import io.github.alexistrejo11.bank.shared.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import io.github.alexistrejo11.bank.shared.result.Result;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class LoanRepaymentHandlersTest {

	@Mock
	LoanRepository loanRepository;

	@Mock
	LoanLedgerOperationsPort ledgerOperationsPort;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@InjectMocks
	PayLoanRepaymentHandler payLoanRepaymentHandler;

	@Test
	@DisplayName("payInstallment returns failure when installment already paid")
	void repayment_already_paid() {
		UUID loanId = UUID.randomUUID();
		UUID repId = UUID.randomUUID();
		UserId userId = UserId.random();
		LoanRepaymentLine rep = new LoanRepaymentLine(repId, 1, LocalDate.now(), new BigDecimal("100"), RepaymentStatus.PAID, Instant.now());
		LoanAggregate loan = baseLoan(loanId, userId, rep);

		when(loanRepository.findWithRepayments(loanId, userId.value())).thenReturn(Optional.of(loan));

		Result<?> r = payLoanRepaymentHandler.handle(userId, new PayLoanRepaymentCommand(loanId, repId));
		assertThat(r.isFailure()).isTrue();
		assertThat(((Result.Failure<?>) r).code()).isEqualTo("REPAYMENT_ALREADY_PAID");
	}

	@Test
	@DisplayName("single repayment on one-installment loan publishes LoanPaidOffEvent")
	void last_payment_paid_off_event() {
		UUID loanId = UUID.randomUUID();
		UUID repId = UUID.randomUUID();
		UUID checking = UUID.randomUUID();
		UserId userId = UserId.random();
		LoanRepaymentLine rep = new LoanRepaymentLine(repId, 1, LocalDate.now(), new BigDecimal("500"), RepaymentStatus.PENDING, null);
		LoanAggregate loan = new LoanAggregate(
				loanId,
				userId.value(),
				checking,
				null,
				new BigDecimal("500"),
				"USD",
				BigDecimal.ZERO,
				1,
				new BigDecimal("500"),
				LoanStatus.ACTIVE,
				Instant.now(),
				Instant.now(),
				List.of(rep)
		);

		when(loanRepository.findWithRepayments(loanId, userId.value())).thenReturn(Optional.of(loan));

		Result<?> r = payLoanRepaymentHandler.handle(userId, new PayLoanRepaymentCommand(loanId, repId));
		assertThat(r.isSuccess()).isTrue();

		verify(ledgerOperationsPort).recordRepayment(eq(loanId), eq(repId), eq(AccountId.of(checking)), eq(new BigDecimal("500")), eq("USD"));

		ArgumentCaptor<Object> cap = ArgumentCaptor.forClass(Object.class);
		verify(eventPublisher).publishEvent(cap.capture());
		assertThat(cap.getValue()).isInstanceOf(LoanPaidOffEvent.class);
	}

	private static LoanAggregate baseLoan(UUID loanId, UserId userId, LoanRepaymentLine rep) {
		return new LoanAggregate(
				loanId,
				userId.value(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				new BigDecimal("1000"),
				"USD",
				new BigDecimal("0.01"),
				3,
				new BigDecimal("100"),
				LoanStatus.ACTIVE,
				Instant.now(),
				Instant.now(),
				List.of(rep)
		);
	}
}
