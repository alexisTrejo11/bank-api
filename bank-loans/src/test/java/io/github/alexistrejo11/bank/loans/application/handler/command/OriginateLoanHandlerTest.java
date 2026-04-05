package io.github.alexistrejo11.bank.loans.application.handler.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.alexistrejo11.bank.loans.domain.command.OriginateLoanCommand;
import io.github.alexistrejo11.bank.loans.domain.model.LoanAggregate;
import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import io.github.alexistrejo11.bank.loans.domain.port.out.CustomerCheckingAccountPort;
import io.github.alexistrejo11.bank.loans.domain.port.out.CustomerCheckingAccountPort.OwnedCheckingAccount;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanRepository;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OriginateLoanHandlerTest {

	@Mock
	LoanRepository loanRepository;

	@Mock
	CustomerCheckingAccountPort checkingAccountPort;

	@InjectMocks
	OriginateLoanHandler handler;

	@Test
	@DisplayName("originate persists loan with repayment rows")
	void originate_saves_schedule() {
		UserId userId = UserId.random();
		UUID checkingId = UUID.randomUUID();
		when(checkingAccountPort.findOwnedChecking(userId, checkingId))
				.thenReturn(Optional.of(new OwnedCheckingAccount(checkingId, "USD")));

		var cmd = new OriginateLoanCommand(checkingId, new BigDecimal("1200"), "usd", new BigDecimal("0.01"), 6);
		handler.handle(userId, cmd);

		ArgumentCaptor<LoanAggregate> cap = ArgumentCaptor.forClass(LoanAggregate.class);
		verify(loanRepository).insert(cap.capture());
		LoanAggregate saved = cap.getValue();
		assertThat(saved.status()).isEqualTo(LoanStatus.PENDING_APPROVAL);
		assertThat(saved.repayments()).hasSize(6);
		assertThat(saved.repayments()).allMatch(r -> r.status() == RepaymentStatus.PENDING);
	}
}
