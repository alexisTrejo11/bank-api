package io.github.alexistrejo11.bank.loans.infrastructure.event;

import io.github.alexistrejo11.bank.shared.event.LoanApprovedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanDisbursedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.event.LoanRepaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Stub until a notifications module exists: logs loan lifecycle for ops visibility.
 */
@Component
public class LoansNotificationListener {

	private static final Logger log = LoggerFactory.getLogger(LoansNotificationListener.class);

	@EventListener
	public void onApproved(LoanApprovedEvent event) {
		log.info("notify_loan_approved loanId={} borrower={} principal={} {}",
				event.loanId().value(),
				event.borrowerId().value(),
				event.principal(),
				event.currencyCode());
	}

	@EventListener
	public void onDisbursed(LoanDisbursedEvent event) {
		log.info("notify_loan_disbursed loanId={} checking={} amount={} {}",
				event.loanId().value(),
				event.checkingAccountId().value(),
				event.amount(),
				event.currencyCode());
	}

	@EventListener
	public void onRepayment(LoanRepaymentCompletedEvent event) {
		log.info("notify_loan_repayment loanId={} repaymentId={} amount={} {}",
				event.loanId().value(),
				event.repaymentId().value(),
				event.amount(),
				event.currencyCode());
	}

	@EventListener
	public void onPaidOff(LoanPaidOffEvent event) {
		log.info("notify_loan_paid_off loanId={}", event.loanId().value());
	}
}
