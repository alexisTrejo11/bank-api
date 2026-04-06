package io.github.alexistrejo11.bank.integration.loans;

import io.github.alexistrejo11.bank.accounts.domain.command.CreateLoanBookkeepingAccountCommand;
import io.github.alexistrejo11.bank.accounts.domain.command.PostTransferToLedgerCommand;
import io.github.alexistrejo11.bank.accounts.domain.port.in.command.CreateLoanBookkeepingAccountUseCase;
import io.github.alexistrejo11.bank.accounts.domain.port.in.command.PostTransferToLedgerUseCase;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanLedgerOperationsPort;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoanLedgerOperationsAdapter implements LoanLedgerOperationsPort {

	private final PostTransferToLedgerUseCase postTransferToLedger;
	private final CreateLoanBookkeepingAccountUseCase createLoanBookkeepingAccount;
	private final UUID internalFundingAccountId;

	public LoanLedgerOperationsAdapter(
			PostTransferToLedgerUseCase postTransferToLedger,
			CreateLoanBookkeepingAccountUseCase createLoanBookkeepingAccount,
			@Value("${bank.loans.internal-funding-account-id}") UUID internalFundingAccountId
	) {
		this.postTransferToLedger = postTransferToLedger;
		this.createLoanBookkeepingAccount = createLoanBookkeepingAccount;
		this.internalFundingAccountId = internalFundingAccountId;
	}

	@Override
	public AccountId createLoanBookkeepingAccount(UserId borrowerId, String currencyCode) {
		return createLoanBookkeepingAccount.execute(new CreateLoanBookkeepingAccountCommand(borrowerId, currencyCode));
	}

	@Override
	public void disbursePrincipal(UUID loanId, AccountId checkingAccountId, BigDecimal amount, String currencyCode) {
		postTransferToLedger.execute(new PostTransferToLedgerCommand(
				AccountId.of(internalFundingAccountId),
				checkingAccountId,
				amount,
				currencyCode,
				"LOAN_DISBURSE",
				loanId
		));
	}

	@Override
	public void recordRepayment(UUID loanId, UUID repaymentId, AccountId checkingAccountId, BigDecimal amount, String currencyCode) {
		postTransferToLedger.execute(new PostTransferToLedgerCommand(
				checkingAccountId,
				AccountId.of(internalFundingAccountId),
				amount,
				currencyCode,
				"LOAN_REPAYMENT",
				repaymentId
		));
	}
}
