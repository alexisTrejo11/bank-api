package io.github.alexistrejo11.bank.loans.domain.port.out;

import io.github.alexistrejo11.bank.loans.domain.model.LoanAggregate;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository {

	void insert(LoanAggregate loan);

	Optional<LoanAggregate> findWithRepayments(UUID loanId, UUID userId);

	void update(LoanAggregate loan);
}
