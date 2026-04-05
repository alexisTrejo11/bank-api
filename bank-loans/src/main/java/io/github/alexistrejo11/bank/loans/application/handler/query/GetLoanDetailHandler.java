package io.github.alexistrejo11.bank.loans.application.handler.query;

import io.github.alexistrejo11.bank.loans.api.dto.response.LoanDetailResponse;
import io.github.alexistrejo11.bank.loans.api.mapper.LoanApiMapper;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanRepository;
import io.github.alexistrejo11.bank.loans.domain.query.GetLoanDetailQuery;
import io.github.alexistrejo11.bank.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetLoanDetailHandler {

	private final LoanRepository loanRepository;

	public GetLoanDetailHandler(LoanRepository loanRepository) {
		this.loanRepository = loanRepository;
	}

	@Transactional(readOnly = true)
	public LoanDetailResponse handle(GetLoanDetailQuery query) {
		return loanRepository.findWithRepayments(query.loanId(), query.userId().value())
				.map(LoanApiMapper::toDetail)
				.orElseThrow(() -> new ResourceNotFoundException("LOAN_NOT_FOUND", "Loan not found"));
	}
}
