package io.github.alexistrejo11.bank.loans.infrastructure.persistence.adapter;

import io.github.alexistrejo11.bank.loans.domain.model.LoanAggregate;
import io.github.alexistrejo11.bank.loans.domain.model.LoanRepaymentLine;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanRepository;
import io.github.alexistrejo11.bank.loans.infrastructure.persistence.entity.LoanEntity;
import io.github.alexistrejo11.bank.loans.infrastructure.persistence.entity.LoanRepaymentEntity;
import io.github.alexistrejo11.bank.loans.infrastructure.persistence.repository.LoanJpaRepository;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LoanRepositoryAdapter implements LoanRepository {

	private final LoanJpaRepository jpaRepository;

	public LoanRepositoryAdapter(LoanJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void insert(LoanAggregate loan) {
		LoanEntity entity = new LoanEntity(
				loan.id(),
				loan.userId(),
				loan.checkingAccountId(),
				loan.loanAccountId(),
				loan.principal(),
				loan.currency(),
				loan.monthlyInterestRate(),
				loan.termMonths(),
				loan.monthlyPayment(),
				loan.status(),
				loan.createdAt(),
				loan.updatedAt()
		);
		for (LoanRepaymentLine r : loan.repayments()) {
			LoanRepaymentEntity re = new LoanRepaymentEntity(
					r.id(),
					r.installmentNumber(),
					r.dueDate(),
					r.amount(),
					r.status(),
					r.paidAt()
			);
			re.setLoan(entity);
			entity.getRepayments().add(re);
		}
		jpaRepository.save(entity);
	}

	@Override
	public Optional<LoanAggregate> findWithRepayments(UUID loanId, UUID userId) {
		return jpaRepository.findByIdAndUserIdWithRepayments(loanId, userId).map(this::toAggregate);
	}

	@Override
	public void update(LoanAggregate loan) {
		LoanEntity e = jpaRepository.findByIdAndUserIdWithRepayments(loan.id(), loan.userId())
				.orElseThrow();
		e.setLoanAccountId(loan.loanAccountId());
		e.setStatus(loan.status());
		e.setUpdatedAt(loan.updatedAt());
		for (LoanRepaymentLine line : loan.repayments()) {
			for (LoanRepaymentEntity re : e.getRepayments()) {
				if (re.getId().equals(line.id())) {
					re.setStatus(line.status());
					re.setPaidAt(line.paidAt());
				}
			}
		}
		jpaRepository.save(e);
	}

	private LoanAggregate toAggregate(LoanEntity e) {
		var reps = e.getRepayments().stream()
				.sorted(Comparator.comparingInt(LoanRepaymentEntity::getInstallmentNumber))
				.map(r -> new LoanRepaymentLine(
						r.getId(),
						r.getInstallmentNumber(),
						r.getDueDate(),
						r.getAmount(),
						r.getStatus(),
						r.getPaidAt()
				))
				.toList();
		return new LoanAggregate(
				e.getId(),
				e.getUserId(),
				e.getCheckingAccountId(),
				e.getLoanAccountId(),
				e.getPrincipal(),
				e.getCurrency(),
				e.getMonthlyInterestRate(),
				e.getTermMonths(),
				e.getMonthlyPayment(),
				e.getStatus(),
				e.getCreatedAt(),
				e.getUpdatedAt(),
				reps
		);
	}
}
