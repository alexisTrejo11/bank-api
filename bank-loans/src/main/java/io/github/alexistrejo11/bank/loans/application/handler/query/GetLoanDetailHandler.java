package io.github.alexistrejo11.bank.loans.application.handler.query;

import io.github.alexistrejo11.bank.loans.domain.model.LoanAggregate;
import io.github.alexistrejo11.bank.loans.domain.repository.LoanRepository;
import io.github.alexistrejo11.bank.loans.application.query.GetLoanDetailQuery;
import io.github.alexistrejo11.bank.shared.shared_kernel.exception.ResourceNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetLoanDetailHandler {

	private final LoanRepository loanRepository;

	public GetLoanDetailHandler(LoanRepository loanRepository) {
		this.loanRepository = loanRepository;
	}

	@Transactional(readOnly = true)
	public LoanAggregate handle(GetLoanDetailQuery query) {
		return loanRepository.findWithRepayments(query.loanId(), query.userId().value())
				.orElseThrow(() -> new ResourceNotFoundException("LOAN_NOT_FOUND", "Loan not found"));
	}
}
