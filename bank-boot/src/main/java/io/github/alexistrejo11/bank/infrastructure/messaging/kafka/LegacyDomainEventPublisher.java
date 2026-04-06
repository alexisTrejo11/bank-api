package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;
import io.github.alexistrejo11.bank.shared.messaging.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * When Kafka is off, publishes {@link BankDomainEvent} on the Spring application event bus after commit
 * (same behaviour as direct {@link ApplicationEventPublisher#publishEvent} with transactional listeners).
 */
public class LegacyDomainEventPublisher implements DomainEventPublisher {

	private final ApplicationEventPublisher applicationEventPublisher;

	public LegacyDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void publishAfterCommit(BankDomainEvent event) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					applicationEventPublisher.publishEvent(event);
				}
			});
		}
		else {
			applicationEventPublisher.publishEvent(event);
		}
	}
}
