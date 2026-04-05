package io.github.alexistrejo11.bank.audit.application;

import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;
import io.github.alexistrejo11.bank.shared.event.LoanApprovedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanDisbursedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.event.LoanRepaymentCompletedEvent;
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
		if (event instanceof LoanApprovedEvent e) {
			return new EntityRef("Loan", e.loanId().value());
		}
		if (event instanceof LoanDisbursedEvent e) {
			return new EntityRef("Loan", e.loanId().value());
		}
		if (event instanceof LoanRepaymentCompletedEvent e) {
			return new EntityRef("LoanRepayment", e.repaymentId().value());
		}
		if (event instanceof LoanPaidOffEvent e) {
			return new EntityRef("Loan", e.loanId().value());
		}
		return new EntityRef("Unknown", null);
	}

	public record EntityRef(String entityType, UUID entityId) {
	}
}
