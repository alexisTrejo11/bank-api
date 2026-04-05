package io.github.alexistrejo11.bank.notifications.infrastructure.event;

import io.github.alexistrejo11.bank.notifications.application.NotificationContentFactory;
import io.github.alexistrejo11.bank.notifications.application.NotificationDispatchService;
import io.github.alexistrejo11.bank.shared.event.LoanApprovedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanDisbursedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.event.LoanRepaymentCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferFailedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferReversedEvent;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BankDomainNotificationListener {

	private final NotificationDispatchService notificationDispatchService;

	public BankDomainNotificationListener(NotificationDispatchService notificationDispatchService) {
		this.notificationDispatchService = notificationDispatchService;
	}

	@EventListener
	public void onTransferCompleted(TransferCompletedEvent event) {
		notificationDispatchService.dispatch(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of("transferId", event.transferId().value().toString())
		);
	}

	@EventListener
	public void onTransferFailed(TransferFailedEvent event) {
		notificationDispatchService.dispatch(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of(
						"transferId", event.transferId().value().toString(),
						"reasonCode", event.reasonCode()
				)
		);
	}

	@EventListener
	public void onTransferReversed(TransferReversedEvent event) {
		notificationDispatchService.dispatch(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of(
						"reversalTransferId", event.reversalTransferId().value().toString(),
						"originalTransferId", event.originalTransferId().value().toString()
				)
		);
	}

	@EventListener
	public void onLoanApproved(LoanApprovedEvent event) {
		UUID uid = event.borrowerId().value();
		notificationDispatchService.dispatch(
				uid,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of("loanId", event.loanId().value().toString(), "userId", uid.toString())
		);
	}

	@EventListener
	public void onLoanDisbursed(LoanDisbursedEvent event) {
		notificationDispatchService.dispatch(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of("loanId", event.loanId().value().toString())
		);
	}

	@EventListener
	public void onLoanRepayment(LoanRepaymentCompletedEvent event) {
		notificationDispatchService.dispatch(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of(
						"loanId", event.loanId().value().toString(),
						"repaymentId", event.repaymentId().value().toString()
				)
		);
	}

	@EventListener
	public void onLoanPaidOff(LoanPaidOffEvent event) {
		notificationDispatchService.dispatch(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of("loanId", event.loanId().value().toString())
		);
	}
}
