package io.github.alexistrejo11.bank.audit.application;

import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferFailedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferReversedEvent;
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
		if (event instanceof TransferFailedEvent e) {
			return new EntityRef("Transfer", e.transferId().value());
		}
		if (event instanceof TransferReversedEvent e) {
			return new EntityRef("Transfer", e.reversalTransferId().value());
		}
		return new EntityRef("Unknown", null);
	}

	public record EntityRef(String entityType, UUID entityId) {
	}
}
