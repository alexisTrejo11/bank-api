package io.github.alexistrejo11.bank.loans.domain.port.out;

import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.math.BigDecimal;
import java.util.UUID;

public interface LoanLedgerOperationsPort {

	AccountId createLoanBookkeepingAccount(UserId borrowerId, String currencyCode);

	void disbursePrincipal(UUID loanId, AccountId checkingAccountId, BigDecimal amount, String currencyCode);

	void recordRepayment(UUID loanId, UUID repaymentId, AccountId checkingAccountId, BigDecimal amount, String currencyCode);
}
