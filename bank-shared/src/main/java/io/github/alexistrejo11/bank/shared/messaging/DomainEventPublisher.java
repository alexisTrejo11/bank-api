package io.github.alexistrejo11.bank.shared.messaging;

import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;

/**
 * Publishes domain events to Kafka after the surrounding transaction commits.
 */
@FunctionalInterface
public interface DomainEventPublisher {

	void publishAfterCommit(BankDomainEvent event);
}
