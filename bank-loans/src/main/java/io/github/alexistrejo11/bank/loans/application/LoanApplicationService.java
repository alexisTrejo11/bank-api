package io.github.alexistrejo11.bank.loans.application;

import io.github.alexistrejo11.bank.loans.api.dto.request.OriginateLoanRequest;
import io.github.alexistrejo11.bank.loans.api.dto.response.LoanDetailResponse;
import io.github.alexistrejo11.bank.loans.api.dto.response.LoanRepaymentItemResponse;
import io.github.alexistrejo11.bank.loans.api.dto.response.PayRepaymentResponse;
import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import io.github.alexistrejo11.bank.loans.domain.port.out.CustomerCheckingAccountPort;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanLedgerOperationsPort;
import io.github.alexistrejo11.bank.loans.domain.service.AmortizationCalculator;
import io.github.alexistrejo11.bank.loans.domain.service.AmortizationCalculator.InstallmentDraft;
import io.github.alexistrejo11.bank.loans.exception.InvalidLoanStateException;
import io.github.alexistrejo11.bank.loans.exception.LoanException;
import io.github.alexistrejo11.bank.loans.infrastructure.persistence.entity.LoanEntity;
import io.github.alexistrejo11.bank.loans.infrastructure.persistence.entity.LoanRepaymentEntity;
import io.github.alexistrejo11.bank.loans.infrastructure.persistence.repository.LoanJpaRepository;
import io.github.alexistrejo11.bank.shared.event.LoanApprovedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanDisbursedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.event.LoanRepaymentCompletedEvent;
import io.github.alexistrejo11.bank.shared.exception.ResourceNotFoundException;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.LoanId;
import io.github.alexistrejo11.bank.shared.ids.LoanRepaymentId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import io.github.alexistrejo11.bank.shared.result.Result;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanApplicationService {

	private final LoanJpaRepository loanRepository;
	private final CustomerCheckingAccountPort checkingAccountPort;
	private final LoanLedgerOperationsPort ledgerOperationsPort;
	private final ApplicationEventPublisher eventPublisher;

	public LoanApplicationService(
			LoanJpaRepository loanRepository,
			CustomerCheckingAccountPort checkingAccountPort,
			LoanLedgerOperationsPort ledgerOperationsPort,
			ApplicationEventPublisher eventPublisher
	) {
		this.loanRepository = loanRepository;
		this.checkingAccountPort = checkingAccountPort;
		this.ledgerOperationsPort = ledgerOperationsPort;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public LoanDetailResponse originate(UserId userId, OriginateLoanRequest request) {
		String ccy = request.currency().trim().toUpperCase();
		var checking = checkingAccountPort.findOwnedChecking(userId, request.checkingAccountId())
				.orElseThrow(() -> new ResourceNotFoundException("LOAN_CHECKING_NOT_FOUND", "Checking account not found or not eligible"));
		if (!checking.currencyCode().equalsIgnoreCase(ccy)) {
			throw new LoanException("LOAN_CURRENCY_MISMATCH", "Loan currency must match checking account currency");
		}
		BigDecimal principal = request.principal().setScale(4, java.math.RoundingMode.HALF_UP);
		BigDecimal monthlyRate = request.monthlyInterestRate();
		int term = request.termMonths();
		BigDecimal monthlyPayment = AmortizationCalculator.monthlyPayment(principal, monthlyRate, term);
		LocalDate firstDue = LocalDate.now(ZoneOffset.UTC).plusMonths(1);
		List<InstallmentDraft> schedule = AmortizationCalculator.buildSchedule(firstDue, principal, monthlyRate, term);

		Instant now = Instant.now();
		UUID loanId = UUID.randomUUID();
		LoanEntity loan = new LoanEntity(
				loanId,
				userId.value(),
				request.checkingAccountId(),
				null,
				principal,
				ccy,
				monthlyRate,
				term,
				monthlyPayment,
				LoanStatus.PENDING_APPROVAL,
				now,
				now
		);
		for (InstallmentDraft row : schedule) {
			LoanRepaymentEntity r = new LoanRepaymentEntity(
					UUID.randomUUID(),
					row.installmentNumber(),
					row.dueDate(),
					row.amount(),
					RepaymentStatus.PENDING,
					null
			);
			r.setLoan(loan);
			loan.getRepayments().add(r);
		}
		loanRepository.save(loan);
		return toDetail(loan);
	}

	@Transactional
	public LoanDetailResponse approve(UserId userId, UUID loanId) {
		LoanEntity loan = loanRepository.findByIdAndUserId(loanId, userId.value())
				.orElseThrow(() -> new ResourceNotFoundException("LOAN_NOT_FOUND", "Loan not found"));
		if (loan.getStatus() != LoanStatus.PENDING_APPROVAL) {
			throw new InvalidLoanStateException("Loan is not pending approval");
		}
		AccountId loanBook = ledgerOperationsPort.createLoanBookkeepingAccount(userId, loan.getCurrency());
		loan.setLoanAccountId(loanBook.value());
		loan.setStatus(LoanStatus.ACTIVE);
		loan.setUpdatedAt(Instant.now());
		loanRepository.save(loan);

		LoanId lid = LoanId.of(loan.getId());
		eventPublisher.publishEvent(new LoanApprovedEvent(
				lid,
				userId,
				AccountId.of(loan.getCheckingAccountId()),
				loanBook,
				loan.getPrincipal(),
				loan.getCurrency(),
				loan.getMonthlyInterestRate(),
				loan.getTermMonths()
		));

		ledgerOperationsPort.disbursePrincipal(loan.getId(), AccountId.of(loan.getCheckingAccountId()), loan.getPrincipal(), loan.getCurrency());
		eventPublisher.publishEvent(new LoanDisbursedEvent(lid, AccountId.of(loan.getCheckingAccountId()), loan.getPrincipal(), loan.getCurrency()));

		return loanRepository.findByIdAndUserIdWithRepayments(loanId, userId.value()).map(this::toDetail).orElseThrow();
	}

	@Transactional(readOnly = true)
	public LoanDetailResponse get(UserId userId, UUID loanId) {
		return loanRepository.findByIdAndUserIdWithRepayments(loanId, userId.value())
				.map(this::toDetail)
				.orElseThrow(() -> new ResourceNotFoundException("LOAN_NOT_FOUND", "Loan not found"));
	}

	@Transactional
	public Result<PayRepaymentResponse> payInstallment(UserId userId, UUID loanId, UUID repaymentId) {
		LoanEntity loan = loanRepository.findByIdAndUserIdWithRepayments(loanId, userId.value())
				.orElseThrow(() -> new ResourceNotFoundException("LOAN_NOT_FOUND", "Loan not found"));
		if (loan.getStatus() != LoanStatus.ACTIVE) {
			return Result.failure("LOAN_NOT_ACTIVE", "Loan must be active to record a repayment");
		}
		LoanRepaymentEntity rep = loan.getRepayments().stream()
				.filter(r -> r.getId().equals(repaymentId))
				.findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("REPAYMENT_NOT_FOUND", "Repayment not found"));
		if (rep.getStatus() == RepaymentStatus.PAID) {
			return Result.failure("REPAYMENT_ALREADY_PAID", "This installment was already paid");
		}
		ledgerOperationsPort.recordRepayment(
				loan.getId(),
				repaymentId,
				AccountId.of(loan.getCheckingAccountId()),
				rep.getAmount(),
				loan.getCurrency()
		);
		Instant now = Instant.now();
		rep.setStatus(RepaymentStatus.PAID);
		rep.setPaidAt(now);
		loan.setUpdatedAt(now);

		boolean allPaid = loan.getRepayments().stream().allMatch(r -> r.getStatus() == RepaymentStatus.PAID);
		if (allPaid) {
			loan.setStatus(LoanStatus.PAID_OFF);
			eventPublisher.publishEvent(new LoanPaidOffEvent(LoanId.of(loan.getId())));
		}
		else {
			eventPublisher.publishEvent(new LoanRepaymentCompletedEvent(
					LoanId.of(loan.getId()),
					LoanRepaymentId.of(repaymentId),
					AccountId.of(loan.getCheckingAccountId()),
					rep.getAmount(),
					loan.getCurrency()
			));
		}
		loanRepository.save(loan);
		return Result.success(new PayRepaymentResponse(repaymentId, RepaymentStatus.PAID, loan.getStatus()));
	}

	private LoanDetailResponse toDetail(LoanEntity loan) {
		List<LoanRepaymentItemResponse> items = loan.getRepayments().stream()
				.sorted(Comparator.comparingInt(LoanRepaymentEntity::getInstallmentNumber))
				.map(r -> new LoanRepaymentItemResponse(
						r.getId(),
						r.getInstallmentNumber(),
						r.getDueDate(),
						r.getAmount(),
						r.getStatus(),
						r.getPaidAt()
				))
				.toList();
		return new LoanDetailResponse(
				loan.getId(),
				loan.getCheckingAccountId(),
				loan.getLoanAccountId(),
				loan.getPrincipal(),
				loan.getCurrency(),
				loan.getMonthlyInterestRate(),
				loan.getTermMonths(),
				loan.getMonthlyPayment(),
				loan.getStatus(),
				loan.getCreatedAt(),
				items
		);
	}
}
