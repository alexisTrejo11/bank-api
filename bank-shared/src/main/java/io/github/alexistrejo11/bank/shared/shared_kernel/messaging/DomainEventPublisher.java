package io.github.alexistrejo11.bank.shared.shared_kernel.messaging;

import io.github.alexistrejo11.bank.shared.shared_kernel.event.BankDomainEvent;

/**
 * Publishes domain events to Kafka after the surrounding transaction commits.
 */
@FunctionalInterface
public interface DomainEventPublisher {

	void publishAfterCommit(BankDomainEvent event);
}
