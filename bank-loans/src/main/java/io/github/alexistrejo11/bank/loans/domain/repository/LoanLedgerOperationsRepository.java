package io.github.alexistrejo11.bank.loans.domain.repository;

import io.github.alexistrejo11.bank.shared.shared_kernel.ids.AccountId;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.math.BigDecimal;
import java.util.UUID;

public interface LoanLedgerOperationsRepository {

	AccountId createLoanBookkeepingAccount(UserId borrowerId, String currencyCode);

	void disbursePrincipal(UUID loanId, AccountId checkingAccountId, BigDecimal amount, String currencyCode);

	void recordRepayment(UUID loanId, UUID repaymentId, AccountId checkingAccountId, BigDecimal amount,
			String currencyCode);
}
