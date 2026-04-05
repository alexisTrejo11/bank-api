package io.github.alexistrejo11.bank.shared.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Superclass for domain events (internal bus). Subclasses add payload fields; publish after successful commits.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class BankDomainEvent {

	private final UUID eventId;
	private final Instant occurredAt;

	protected BankDomainEvent() {
		this(UUID.randomUUID(), Instant.now());
	}

	protected BankDomainEvent(UUID eventId, Instant occurredAt) {
		this.eventId = Objects.requireNonNullElseGet(eventId, UUID::randomUUID);
		this.occurredAt = Objects.requireNonNullElseGet(occurredAt, Instant::now);
	}

	public UUID eventId() {
		return eventId;
	}

	public Instant occurredAt() {
		return occurredAt;
	}
}
