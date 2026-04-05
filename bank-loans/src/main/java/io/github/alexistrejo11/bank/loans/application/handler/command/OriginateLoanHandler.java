package io.github.alexistrejo11.bank.loans.application.handler.command;

import io.github.alexistrejo11.bank.loans.api.dto.response.LoanDetailResponse;
import io.github.alexistrejo11.bank.loans.api.mapper.LoanApiMapper;
import io.github.alexistrejo11.bank.loans.domain.command.OriginateLoanCommand;
import io.github.alexistrejo11.bank.loans.domain.exception.LoanException;
import io.github.alexistrejo11.bank.loans.domain.model.LoanAggregate;
import io.github.alexistrejo11.bank.loans.domain.model.LoanRepaymentLine;
import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import io.github.alexistrejo11.bank.loans.domain.port.out.CustomerCheckingAccountPort;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanRepository;
import io.github.alexistrejo11.bank.loans.domain.service.AmortizationCalculator;
import io.github.alexistrejo11.bank.loans.domain.service.AmortizationCalculator.InstallmentDraft;
import io.github.alexistrejo11.bank.shared.exception.ResourceNotFoundException;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OriginateLoanHandler {

	private final LoanRepository loanRepository;
	private final CustomerCheckingAccountPort checkingAccountPort;

	public OriginateLoanHandler(LoanRepository loanRepository, CustomerCheckingAccountPort checkingAccountPort) {
		this.loanRepository = loanRepository;
		this.checkingAccountPort = checkingAccountPort;
	}

	@Transactional
	public LoanDetailResponse handle(UserId userId, OriginateLoanCommand command) {
		String ccy = command.currency().trim().toUpperCase();
		var checking = checkingAccountPort.findOwnedChecking(userId, command.checkingAccountId())
				.orElseThrow(() -> new ResourceNotFoundException("LOAN_CHECKING_NOT_FOUND", "Checking account not found or not eligible"));
		if (!checking.currencyCode().equalsIgnoreCase(ccy)) {
			throw new LoanException("LOAN_CURRENCY_MISMATCH", "Loan currency must match checking account currency");
		}
		BigDecimal principal = command.principal().setScale(4, java.math.RoundingMode.HALF_UP);
		BigDecimal monthlyRate = command.monthlyInterestRate();
		int term = command.termMonths();
		BigDecimal monthlyPayment = AmortizationCalculator.monthlyPayment(principal, monthlyRate, term);
		LocalDate firstDue = LocalDate.now(ZoneOffset.UTC).plusMonths(1);
		List<InstallmentDraft> schedule = AmortizationCalculator.buildSchedule(firstDue, principal, monthlyRate, term);

		Instant now = Instant.now();
		UUID loanId = UUID.randomUUID();
		List<LoanRepaymentLine> repayments = new ArrayList<>();
		for (InstallmentDraft row : schedule) {
			repayments.add(new LoanRepaymentLine(
					UUID.randomUUID(),
					row.installmentNumber(),
					row.dueDate(),
					row.amount(),
					RepaymentStatus.PENDING,
					null
			));
		}
		LoanAggregate loan = new LoanAggregate(
				loanId,
				userId.value(),
				command.checkingAccountId(),
				null,
				principal,
				ccy,
				monthlyRate,
				term,
				monthlyPayment,
				LoanStatus.PENDING_APPROVAL,
				now,
				now,
				repayments
		);
		loanRepository.insert(loan);
		return LoanApiMapper.toDetail(loan);
	}
}
