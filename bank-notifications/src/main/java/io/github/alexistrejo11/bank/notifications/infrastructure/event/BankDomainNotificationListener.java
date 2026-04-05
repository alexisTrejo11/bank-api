package io.github.alexistrejo11.bank.notifications.infrastructure.event;

import io.github.alexistrejo11.bank.notifications.application.handler.command.DispatchNotificationHandler;
import io.github.alexistrejo11.bank.notifications.domain.command.DispatchNotificationCommand;
import io.github.alexistrejo11.bank.notifications.domain.service.NotificationContentFactory;
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

	private final DispatchNotificationHandler dispatchNotificationHandler;

	public BankDomainNotificationListener(DispatchNotificationHandler dispatchNotificationHandler) {
		this.dispatchNotificationHandler = dispatchNotificationHandler;
	}

	@EventListener
	public void onTransferCompleted(TransferCompletedEvent event) {
		dispatchNotificationHandler.handle(new DispatchNotificationCommand(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of("transferId", event.transferId().value().toString())
		));
	}

	@EventListener
	public void onTransferFailed(TransferFailedEvent event) {
		dispatchNotificationHandler.handle(new DispatchNotificationCommand(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of(
						"transferId", event.transferId().value().toString(),
						"reasonCode", event.reasonCode()
				)
		));
	}

	@EventListener
	public void onTransferReversed(TransferReversedEvent event) {
		dispatchNotificationHandler.handle(new DispatchNotificationCommand(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of(
						"reversalTransferId", event.reversalTransferId().value().toString(),
						"originalTransferId", event.originalTransferId().value().toString()
				)
		));
	}

	@EventListener
	public void onLoanApproved(LoanApprovedEvent event) {
		UUID uid = event.borrowerId().value();
		dispatchNotificationHandler.handle(new DispatchNotificationCommand(
				uid,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of("loanId", event.loanId().value().toString(), "userId", uid.toString())
		));
	}

	@EventListener
	public void onLoanDisbursed(LoanDisbursedEvent event) {
		dispatchNotificationHandler.handle(new DispatchNotificationCommand(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of("loanId", event.loanId().value().toString())
		));
	}

	@EventListener
	public void onLoanRepayment(LoanRepaymentCompletedEvent event) {
		dispatchNotificationHandler.handle(new DispatchNotificationCommand(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of(
						"loanId", event.loanId().value().toString(),
						"repaymentId", event.repaymentId().value().toString()
				)
		));
	}

	@EventListener
	public void onLoanPaidOff(LoanPaidOffEvent event) {
		dispatchNotificationHandler.handle(new DispatchNotificationCommand(
				null,
				event.getClass().getSimpleName(),
				NotificationContentFactory.from(event),
				Map.of("loanId", event.loanId().value().toString())
		));
	}
}
