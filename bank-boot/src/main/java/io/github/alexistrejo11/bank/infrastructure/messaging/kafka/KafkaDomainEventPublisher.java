package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;
import io.github.alexistrejo11.bank.shared.event.LoanApprovedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanDisbursedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.event.LoanRepaymentCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferFailedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferReversedEvent;
import io.github.alexistrejo11.bank.shared.messaging.BankKafkaTopics;
import io.github.alexistrejo11.bank.shared.messaging.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publishes domain events to Kafka after commit. Fan-out to {@link BankKafkaTopics#NOTIFICATIONS} for
 * the same event types that previously fed {@code @EventListener} notification handlers.
 */
public class KafkaDomainEventPublisher implements DomainEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

	private final KafkaTemplate<String, BankDomainEvent> kafkaTemplate;

	public KafkaDomainEventPublisher(KafkaTemplate<String, BankDomainEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void publishAfterCommit(BankDomainEvent event) {
		Runnable send = () -> {
			String domainTopic = domainTopic(event);
			String domainKey = domainPartitionKey(event);
			kafkaTemplate.send(domainTopic, domainKey, event).whenComplete((r, ex) -> {
				if (ex != null) {
					log.error("kafka_send_failed topic={} key={} eventType={}", domainTopic, domainKey, event.getClass().getSimpleName(), ex);
				}
			});
			if (fanOutToNotifications(event)) {
				String nKey = notificationPartitionKey(event);
				kafkaTemplate.send(BankKafkaTopics.NOTIFICATIONS, nKey, event).whenComplete((r, ex) -> {
					if (ex != null) {
						log.error("kafka_send_failed topic={} key={}", BankKafkaTopics.NOTIFICATIONS, nKey, ex);
					}
				});
			}
		};
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					send.run();
				}
			});
		}
		else {
			send.run();
		}
	}

	private static String domainTopic(BankDomainEvent event) {
		if (event instanceof TransferCompletedEvent || event instanceof TransferFailedEvent || event instanceof TransferReversedEvent) {
			return BankKafkaTopics.TRANSFERS;
		}
		if (event instanceof LoanApprovedEvent
				|| event instanceof LoanDisbursedEvent
				|| event instanceof LoanRepaymentCompletedEvent
				|| event instanceof LoanPaidOffEvent) {
			return BankKafkaTopics.LOANS;
		}
		throw new IllegalArgumentException("Unsupported domain event: " + event.getClass().getName());
	}

	private static String domainPartitionKey(BankDomainEvent event) {
		return switch (event) {
			case TransferCompletedEvent e -> e.transferId().value().toString();
			case TransferFailedEvent e -> e.transferId().value().toString();
			case TransferReversedEvent e -> e.reversalTransferId().value().toString();
			case LoanApprovedEvent e -> e.loanId().value().toString();
			case LoanDisbursedEvent e -> e.loanId().value().toString();
			case LoanRepaymentCompletedEvent e -> e.loanId().value().toString();
			case LoanPaidOffEvent e -> e.loanId().value().toString();
			default -> event.eventId().toString();
		};
	}

	private static boolean fanOutToNotifications(BankDomainEvent event) {
		return event instanceof TransferCompletedEvent
				|| event instanceof TransferFailedEvent
				|| event instanceof TransferReversedEvent
				|| event instanceof LoanApprovedEvent
				|| event instanceof LoanDisbursedEvent
				|| event instanceof LoanRepaymentCompletedEvent
				|| event instanceof LoanPaidOffEvent;
	}

	private static String notificationPartitionKey(BankDomainEvent event) {
		if (event instanceof LoanApprovedEvent e) {
			return e.borrowerId().value().toString();
		}
		if (event instanceof TransferCompletedEvent e) {
			return e.transferId().value().toString();
		}
		if (event instanceof TransferFailedEvent e) {
			return e.transferId().value().toString();
		}
		if (event instanceof TransferReversedEvent e) {
			return e.reversalTransferId().value().toString();
		}
		if (event instanceof LoanDisbursedEvent e) {
			return e.loanId().value().toString();
		}
		if (event instanceof LoanRepaymentCompletedEvent e) {
			return e.loanId().value().toString();
		}
		if (event instanceof LoanPaidOffEvent e) {
			return e.loanId().value().toString();
		}
		return event.eventId().toString();
	}
}
