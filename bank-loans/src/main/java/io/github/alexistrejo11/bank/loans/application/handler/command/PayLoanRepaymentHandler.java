package io.github.alexistrejo11.bank.loans.application.handler.command;

import io.github.alexistrejo11.bank.loans.application.command.PayLoanRepaymentCommand;
import io.github.alexistrejo11.bank.loans.domain.model.LoanAggregate;
import io.github.alexistrejo11.bank.loans.domain.model.LoanRepaymentLine;
import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import io.github.alexistrejo11.bank.loans.domain.repository.LoanLedgerOperationsRepository;
import io.github.alexistrejo11.bank.loans.domain.repository.LoanRepository;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.AccountId;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.LoanId;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.LoanRepaymentId;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import io.github.alexistrejo11.bank.shared.shared_kernel.result.Result;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.LoanRepaymentCompletedEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PayLoanRepaymentHandler {

	private final LoanRepository loanRepository;
	private final LoanLedgerOperationsRepository ledgerOperationsPort;
	private final ApplicationEventPublisher eventPublisher;

	public PayLoanRepaymentHandler(
			LoanRepository loanRepository,
			LoanLedgerOperationsRepository ledgerOperationsPort,
			ApplicationEventPublisher eventPublisher) {
		this.loanRepository = loanRepository;
		this.ledgerOperationsPort = ledgerOperationsPort;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public Result<LoanAggregate> handle(UserId userId, PayLoanRepaymentCommand command) {
		LoanAggregate loan = loanRepository.findWithRepayments(command.loanId(), userId.value())
				.orElseThrow(() -> new ResourceNotFoundException("LOAN_NOT_FOUND", "Loan not found"));
		if (loan.status() != LoanStatus.ACTIVE) {
			return Result.failure("LOAN_NOT_ACTIVE", "Loan must be active to record a repayment");
		}
		UUID repaymentId = command.repaymentId();
		LoanRepaymentLine rep = loan.repayments().stream()
				.filter(r -> r.id().equals(repaymentId))
				.findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("REPAYMENT_NOT_FOUND", "Repayment not found"));
		if (rep.status() == RepaymentStatus.PAID) {
			return Result.failure("REPAYMENT_ALREADY_PAID", "This installment was already paid");
		}
		ledgerOperationsPort.recordRepayment(
				loan.id(),
				repaymentId,
				AccountId.of(loan.checkingAccountId()),
				rep.amount(),
				loan.currency());
		Instant now = Instant.now();
		List<LoanRepaymentLine> newRepayments = loan.repayments().stream()
				.map(r -> r.id().equals(repaymentId) ? r.withPaid(now) : r)
				.toList();
		boolean allPaid = newRepayments.stream().allMatch(r -> r.status() == RepaymentStatus.PAID);
		LoanStatus newStatus = allPaid ? LoanStatus.PAID_OFF : LoanStatus.ACTIVE;
		LoanAggregate updated = loan.withRepaymentsAndStatus(newRepayments, newStatus, now);

		if (allPaid) {
			eventPublisher.publishEvent(new LoanPaidOffEvent(LoanId.of(loan.id())));
		} else {
			eventPublisher.publishEvent(new LoanRepaymentCompletedEvent(
					LoanId.of(loan.id()),
					LoanRepaymentId.of(repaymentId),
					AccountId.of(loan.checkingAccountId()),
					rep.amount(),
					loan.currency()));
		}
		loanRepository.update(updated);
		return Result.success(updated);
	}
}
