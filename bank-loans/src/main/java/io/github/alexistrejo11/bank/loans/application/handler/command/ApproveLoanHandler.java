package io.github.alexistrejo11.bank.loans.application.handler.command;

import io.github.alexistrejo11.bank.loans.api.dto.response.LoanDetailResponse;
import io.github.alexistrejo11.bank.loans.api.mapper.LoanApiMapper;
import io.github.alexistrejo11.bank.loans.domain.command.ApproveLoanCommand;
import io.github.alexistrejo11.bank.loans.domain.exception.InvalidLoanStateException;
import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanLedgerOperationsPort;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanRepository;
import io.github.alexistrejo11.bank.shared.event.LoanApprovedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanDisbursedEvent;
import io.github.alexistrejo11.bank.shared.exception.ResourceNotFoundException;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.LoanId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApproveLoanHandler {

	private final LoanRepository loanRepository;
	private final LoanLedgerOperationsPort ledgerOperationsPort;
	private final ApplicationEventPublisher eventPublisher;

	public ApproveLoanHandler(
			LoanRepository loanRepository,
			LoanLedgerOperationsPort ledgerOperationsPort,
			ApplicationEventPublisher eventPublisher
	) {
		this.loanRepository = loanRepository;
		this.ledgerOperationsPort = ledgerOperationsPort;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public LoanDetailResponse handle(UserId userId, ApproveLoanCommand command) {
		var loan = loanRepository.findWithRepayments(command.loanId(), userId.value())
				.orElseThrow(() -> new ResourceNotFoundException("LOAN_NOT_FOUND", "Loan not found"));
		if (loan.status() != LoanStatus.PENDING_APPROVAL) {
			throw new InvalidLoanStateException("Loan is not pending approval");
		}
		Instant now = Instant.now();
		AccountId loanBook = ledgerOperationsPort.createLoanBookkeepingAccount(userId, loan.currency());
		var approved = loan.withApproval(loanBook.value(), now);
		loanRepository.update(approved);

		LoanId lid = LoanId.of(loan.id());
		eventPublisher.publishEvent(new LoanApprovedEvent(
				lid,
				userId,
				AccountId.of(loan.checkingAccountId()),
				loanBook,
				loan.principal(),
				loan.currency(),
				loan.monthlyInterestRate(),
				loan.termMonths()
		));

		ledgerOperationsPort.disbursePrincipal(loan.id(), AccountId.of(loan.checkingAccountId()), loan.principal(), loan.currency());
		eventPublisher.publishEvent(new LoanDisbursedEvent(lid, AccountId.of(loan.checkingAccountId()), loan.principal(), loan.currency()));

		return loanRepository.findWithRepayments(command.loanId(), userId.value())
				.map(LoanApiMapper::toDetail)
				.orElseThrow();
	}
}
