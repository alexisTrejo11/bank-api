package io.github.alexistrejo11.bank.shared.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Superclass for domain events. Subclasses add payload fields; publish after successful commits.
 * Polymorphic JSON for Kafka uses {@code eventType} (see {@link JsonTypeInfo}).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = TransferCompletedEvent.class, name = "TransferCompleted"),
		@JsonSubTypes.Type(value = TransferFailedEvent.class, name = "TransferFailed"),
		@JsonSubTypes.Type(value = TransferReversedEvent.class, name = "TransferReversed"),
		@JsonSubTypes.Type(value = LoanApprovedEvent.class, name = "LoanApproved"),
		@JsonSubTypes.Type(value = LoanDisbursedEvent.class, name = "LoanDisbursed"),
		@JsonSubTypes.Type(value = LoanRepaymentCompletedEvent.class, name = "LoanRepaymentCompleted"),
		@JsonSubTypes.Type(value = LoanPaidOffEvent.class, name = "LoanPaidOff"),
})
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
