package io.github.alexistrejo11.bank.accounts.infrastructure.event;

import io.github.alexistrejo11.bank.accounts.application.LedgerPostingService;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies ledger postings when the payments module publishes {@link TransferCompletedEvent}.
 */
@Component
public class AccountsTransferListener {

	private final LedgerPostingService ledgerPostingService;

	public AccountsTransferListener(LedgerPostingService ledgerPostingService) {
		this.ledgerPostingService = ledgerPostingService;
	}

	@EventListener
	@Transactional
	public void onTransferCompleted(TransferCompletedEvent event) {
		ledgerPostingService.postTransfer(
				event.sourceAccountId(),
				event.targetAccountId(),
				event.amount(),
				event.currencyCode(),
				"TRANSFER",
				event.transferId().value()
		);
	}
}
