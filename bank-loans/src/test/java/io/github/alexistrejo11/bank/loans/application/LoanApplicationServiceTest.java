package io.github.alexistrejo11.bank.loans.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.alexistrejo11.bank.loans.api.dto.request.OriginateLoanRequest;
import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import io.github.alexistrejo11.bank.loans.domain.port.out.CustomerCheckingAccountPort;
import io.github.alexistrejo11.bank.loans.domain.port.out.CustomerCheckingAccountPort.OwnedCheckingAccount;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanLedgerOperationsPort;
import io.github.alexistrejo11.bank.loans.infrastructure.persistence.entity.LoanEntity;
import io.github.alexistrejo11.bank.loans.infrastructure.persistence.entity.LoanRepaymentEntity;
import io.github.alexistrejo11.bank.loans.infrastructure.persistence.repository.LoanJpaRepository;
import io.github.alexistrejo11.bank.shared.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.event.LoanRepaymentCompletedEvent;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import io.github.alexistrejo11.bank.shared.result.Result;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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
class LoanApplicationServiceTest {

	@Mock
	LoanJpaRepository loanRepository;

	@Mock
	CustomerCheckingAccountPort checkingAccountPort;

	@Mock
	LoanLedgerOperationsPort ledgerOperationsPort;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@InjectMocks
	LoanApplicationService service;

	@Test
	@DisplayName("payInstallment returns failure when installment already paid")
	void repayment_already_paid() {
		UUID loanId = UUID.randomUUID();
		UUID repId = UUID.randomUUID();
		UserId userId = UserId.random();
		LoanEntity loan = new LoanEntity(
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
				Instant.now()
		);
		LoanRepaymentEntity rep = new LoanRepaymentEntity(repId, 1, LocalDate.now(), new BigDecimal("100"), RepaymentStatus.PAID, Instant.now());
		rep.setLoan(loan);
		loan.getRepayments().add(rep);

		when(loanRepository.findByIdAndUserIdWithRepayments(loanId, userId.value())).thenReturn(Optional.of(loan));

		Result<?> r = service.payInstallment(userId, loanId, repId);
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
		LoanEntity loan = new LoanEntity(
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
				Instant.now()
		);
		LoanRepaymentEntity rep = new LoanRepaymentEntity(repId, 1, LocalDate.now(), new BigDecimal("500"), RepaymentStatus.PENDING, null);
		rep.setLoan(loan);
		loan.getRepayments().add(rep);

		when(loanRepository.findByIdAndUserIdWithRepayments(loanId, userId.value())).thenReturn(Optional.of(loan));

		Result<?> r = service.payInstallment(userId, loanId, repId);
		assertThat(r.isSuccess()).isTrue();

		verify(ledgerOperationsPort).recordRepayment(eq(loanId), eq(repId), eq(AccountId.of(checking)), eq(new BigDecimal("500")), eq("USD"));

		ArgumentCaptor<Object> cap = ArgumentCaptor.forClass(Object.class);
		verify(eventPublisher).publishEvent(cap.capture());
		List<Object> events = new ArrayList<>(cap.getAllValues());
		assertThat(events).anyMatch(e -> e instanceof LoanPaidOffEvent);
		assertThat(events).noneMatch(e -> e instanceof LoanRepaymentCompletedEvent);
	}

	@Test
	@DisplayName("originate persists loan with repayment rows")
	void originate_saves_schedule() {
		UserId userId = UserId.random();
		UUID checkingId = UUID.randomUUID();
		when(checkingAccountPort.findOwnedChecking(userId, checkingId))
				.thenReturn(Optional.of(new OwnedCheckingAccount(checkingId, "USD")));

		var req = new OriginateLoanRequest(checkingId, new BigDecimal("1200"), "usd", new BigDecimal("0.01"), 6);
		service.originate(userId, req);

		ArgumentCaptor<LoanEntity> cap = ArgumentCaptor.forClass(LoanEntity.class);
		verify(loanRepository).save(cap.capture());
		LoanEntity saved = cap.getValue();
		assertThat(saved.getStatus()).isEqualTo(LoanStatus.PENDING_APPROVAL);
		assertThat(saved.getRepayments()).hasSize(6);
		assertThat(saved.getRepayments()).allMatch(r -> r.getStatus() == RepaymentStatus.PENDING);
	}
}
