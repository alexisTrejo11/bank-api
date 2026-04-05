package io.github.alexistrejo11.bank.integration.loans;

import io.github.alexistrejo11.bank.accounts.application.LedgerPostingService;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.AccountEntity;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import io.github.alexistrejo11.bank.loans.domain.port.out.LoanLedgerOperationsPort;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoanLedgerOperationsAdapter implements LoanLedgerOperationsPort {

	private final LedgerPostingService ledgerPostingService;
	private final AccountJpaRepository accountRepository;
	private final UUID internalFundingAccountId;

	public LoanLedgerOperationsAdapter(
			LedgerPostingService ledgerPostingService,
			AccountJpaRepository accountRepository,
			@Value("${bank.loans.internal-funding-account-id}") UUID internalFundingAccountId
	) {
		this.ledgerPostingService = ledgerPostingService;
		this.accountRepository = accountRepository;
		this.internalFundingAccountId = internalFundingAccountId;
	}

	@Override
	public AccountId createLoanBookkeepingAccount(UserId borrowerId, String currencyCode) {
		Instant now = Instant.now();
		UUID id = UUID.randomUUID();
		String ccy = currencyCode.toUpperCase();
		accountRepository.save(new AccountEntity(id, borrowerId.value(), AccountType.LOAN, ccy, AccountStatus.ACTIVE, now, now));
		return AccountId.of(id);
	}

	@Override
	public void disbursePrincipal(UUID loanId, AccountId checkingAccountId, BigDecimal amount, String currencyCode) {
		ledgerPostingService.postTransfer(
				AccountId.of(internalFundingAccountId),
				checkingAccountId,
				amount,
				currencyCode,
				"LOAN_DISBURSE",
				loanId
		);
	}

	@Override
	public void recordRepayment(UUID loanId, UUID repaymentId, AccountId checkingAccountId, BigDecimal amount, String currencyCode) {
		ledgerPostingService.postTransfer(
				checkingAccountId,
				AccountId.of(internalFundingAccountId),
				amount,
				currencyCode,
				"LOAN_REPAYMENT",
				repaymentId
		);
	}
}
