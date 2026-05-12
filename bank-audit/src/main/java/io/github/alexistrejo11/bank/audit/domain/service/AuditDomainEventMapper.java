package io.github.alexistrejo11.bank.audit.domain.service;

import java.util.UUID;

import io.github.alexistrejo11.bank.shared.shared_kernel.event.BankDomainEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.LoanApprovedEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.LoanDisbursedEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.LoanRepaymentCompletedEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.TransferFailedEvent;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.TransferReversedEvent;

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
