package io.github.alexistrejo11.bank.loans.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.alexistrejo11.bank.shared.shared_kernel.event.LoanApprovedEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.LoanDisbursedEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.LoanRepaymentCompletedEvent;

/**
 * Stub until a notifications module exists: logs loan lifecycle for ops
 * visibility.
 */
@Component
public class LoansNotificationListener {

	private static final Logger log = LoggerFactory.getLogger(LoansNotificationListener.class);

	@EventListener
	public void onApproved(LoanApprovedEvent event) {
		log.debug("notify_loan_approved loanId={} borrower={} principal={} {}",
				event.loanId().value(),
				event.borrowerId().value(),
				event.principal(),
				event.currencyCode());
	}

	@EventListener
	public void onDisbursed(LoanDisbursedEvent event) {
		log.debug("notify_loan_disbursed loanId={} checking={} amount={} {}",
				event.loanId().value(),
				event.checkingAccountId().value(),
				event.amount(),
				event.currencyCode());
	}

	@EventListener
	public void onRepayment(LoanRepaymentCompletedEvent event) {
		log.debug("notify_loan_repayment loanId={} repaymentId={} amount={} {}",
				event.loanId().value(),
				event.repaymentId().value(),
				event.amount(),
				event.currencyCode());
	}

	@EventListener
	public void onPaidOff(LoanPaidOffEvent event) {
		log.debug("notify_loan_paid_off loanId={}", event.loanId().value());
	}
}
