package io.github.alexistrejo11.bank.notifications.domain.service;

import io.github.alexistrejo11.bank.notifications.domain.model.GenericEmailContent;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationTemplateKey;
import io.github.alexistrejo11.bank.shared.event.LoanApprovedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanDisbursedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanPaidOffEvent;
import io.github.alexistrejo11.bank.shared.event.LoanRepaymentCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferFailedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferReversedEvent;
import java.util.List;

public final class NotificationContentFactory {

	private NotificationContentFactory() {
	}

	public static GenericEmailContent from(TransferCompletedEvent e) {
		return new GenericEmailContent(
				"Transfer completed",
				"Money has moved successfully between your accounts.",
				List.of(
						"Amount: " + e.amount() + " " + e.currencyCode(),
						"From account: " + e.sourceAccountId().value(),
						"To account: " + e.targetAccountId().value()
				),
				"View activity",
				null,
				NotificationTemplateKey.GENERIC_MESSAGE
		);
	}

	public static GenericEmailContent from(TransferFailedEvent e) {
		return new GenericEmailContent(
				"Transfer could not be completed",
				"We were unable to process a transfer you initiated.",
				List.of("Reason: " + e.message(), "Code: " + e.reasonCode()),
				null,
				null,
				NotificationTemplateKey.GENERIC_ALERT
		);
	}

	public static GenericEmailContent from(TransferReversedEvent e) {
		return new GenericEmailContent(
				"Transfer reversed",
				"A previous transfer was reversed on your account.",
				List.of(
						"Amount: " + e.amount() + " " + e.currencyCode(),
						"Original transfer: " + e.originalTransferId().value()
				),
				null,
				null,
				NotificationTemplateKey.GENERIC_ALERT
		);
	}

	public static GenericEmailContent from(LoanApprovedEvent e) {
		return new GenericEmailContent(
				"Loan approved",
				"Your loan application has been approved and booked.",
				List.of(
						"Principal: " + e.principal() + " " + e.currencyCode(),
						"Term: " + e.termMonths() + " months",
						"Loan id: " + e.loanId().value()
				),
				null,
				null,
				NotificationTemplateKey.GENERIC_MESSAGE
		);
	}

	public static GenericEmailContent from(LoanDisbursedEvent e) {
		return new GenericEmailContent(
				"Loan disbursed",
				"Funds from your loan have been credited to your checking account.",
				List.of(
						"Amount: " + e.amount() + " " + e.currencyCode(),
						"Loan id: " + e.loanId().value()
				),
				null,
				null,
				NotificationTemplateKey.GENERIC_MESSAGE
		);
	}

	public static GenericEmailContent from(LoanRepaymentCompletedEvent e) {
		return new GenericEmailContent(
				"Loan payment received",
				"We recorded an installment payment on your loan.",
				List.of(
						"Amount: " + e.amount() + " " + e.currencyCode(),
						"Loan id: " + e.loanId().value()
				),
				null,
				null,
				NotificationTemplateKey.GENERIC_MESSAGE
		);
	}

	public static GenericEmailContent from(LoanPaidOffEvent e) {
		return new GenericEmailContent(
				"Loan paid off",
				"Congratulations — your loan balance is fully repaid.",
				List.of("Loan id: " + e.loanId().value()),
				null,
				null,
				NotificationTemplateKey.GENERIC_MESSAGE
		);
	}

	public static String smsSummary(GenericEmailContent c) {
		StringBuilder sb = new StringBuilder();
		sb.append(c.title()).append(". ").append(c.lead());
		if (!c.detailLines().isEmpty()) {
			sb.append(" ").append(c.detailLines().getFirst());
		}
		String s = sb.toString();
		return s.length() > 320 ? s.substring(0, 317) + "..." : s;
	}
}
