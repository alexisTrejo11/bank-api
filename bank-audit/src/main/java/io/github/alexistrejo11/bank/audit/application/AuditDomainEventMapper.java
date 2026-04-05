package io.github.alexistrejo11.bank.audit.application;

import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import java.util.UUID;

public final class AuditDomainEventMapper {

	private AuditDomainEventMapper() {
	}

	public static String eventType(BankDomainEvent event) {
		return event.getClass().getSimpleName();
	}

	public static EntityRef entityRef(BankDomainEvent event) {
		if (event instanceof TransferCompletedEvent e) {
			return new EntityRef("Transfer", e.transferId().value());
		}
		return new EntityRef("Unknown", null);
	}

	public record EntityRef(String entityType, UUID entityId) {
	}
}
