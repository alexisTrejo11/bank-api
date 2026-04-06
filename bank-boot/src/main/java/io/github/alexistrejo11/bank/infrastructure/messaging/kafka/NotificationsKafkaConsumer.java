package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import io.github.alexistrejo11.bank.notifications.domain.command.DispatchNotificationCommand;
import io.github.alexistrejo11.bank.notifications.domain.service.NotificationContentFactory;
import io.github.alexistrejo11.bank.notifications.infrastructure.messaging.NotificationDispatchIngress;
import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;
import io.github.alexistrejo11.bank.shared.event.LoanApprovedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanDisbursedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.event.LoanRepaymentCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferFailedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferReversedEvent;
import io.github.alexistrejo11.bank.shared.messaging.BankKafkaTopics;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Dispatches notifications from the dedicated notifications topic (same payloads as domain events).
 */
@Component
@ConditionalOnProperty(prefix = "bank.kafka", name = "enabled", havingValue = "true")
public class NotificationsKafkaConsumer {

	private final NotificationDispatchIngress notificationDispatchIngress;

	public NotificationsKafkaConsumer(NotificationDispatchIngress notificationDispatchIngress) {
		this.notificationDispatchIngress = notificationDispatchIngress;
	}

	@KafkaListener(
			topics = BankKafkaTopics.NOTIFICATIONS,
			groupId = "notifications-cg",
			containerFactory = "bankDomainEventKafkaListenerContainerFactory"
	)
	public void onNotification(BankDomainEvent event) {
		switch (event) {
			case TransferCompletedEvent e -> notificationDispatchIngress.submit(new DispatchNotificationCommand(
					null,
					e.getClass().getSimpleName(),
					NotificationContentFactory.from(e),
					Map.of("transferId", e.transferId().value().toString())
			));
			case TransferFailedEvent e -> notificationDispatchIngress.submit(new DispatchNotificationCommand(
					null,
					e.getClass().getSimpleName(),
					NotificationContentFactory.from(e),
					Map.of(
							"transferId", e.transferId().value().toString(),
							"reasonCode", e.reasonCode()
					)
			));
			case TransferReversedEvent e -> notificationDispatchIngress.submit(new DispatchNotificationCommand(
					null,
					e.getClass().getSimpleName(),
					NotificationContentFactory.from(e),
					Map.of(
							"reversalTransferId", e.reversalTransferId().value().toString(),
							"originalTransferId", e.originalTransferId().value().toString()
					)
			));
			case LoanApprovedEvent e -> {
				UUID uid = e.borrowerId().value();
				notificationDispatchIngress.submit(new DispatchNotificationCommand(
						uid,
						e.getClass().getSimpleName(),
						NotificationContentFactory.from(e),
						Map.of("loanId", e.loanId().value().toString(), "userId", uid.toString())
				));
			}
			case LoanDisbursedEvent e -> notificationDispatchIngress.submit(new DispatchNotificationCommand(
					null,
					e.getClass().getSimpleName(),
					NotificationContentFactory.from(e),
					Map.of("loanId", e.loanId().value().toString())
			));
			case LoanRepaymentCompletedEvent e -> notificationDispatchIngress.submit(new DispatchNotificationCommand(
					null,
					e.getClass().getSimpleName(),
					NotificationContentFactory.from(e),
					Map.of(
							"loanId", e.loanId().value().toString(),
							"repaymentId", e.repaymentId().value().toString()
					)
			));
			case LoanPaidOffEvent e -> notificationDispatchIngress.submit(new DispatchNotificationCommand(
					null,
					e.getClass().getSimpleName(),
					NotificationContentFactory.from(e),
					Map.of("loanId", e.loanId().value().toString())
			));
			default -> {
				// ignore unknown
			}
		}
	}
}
