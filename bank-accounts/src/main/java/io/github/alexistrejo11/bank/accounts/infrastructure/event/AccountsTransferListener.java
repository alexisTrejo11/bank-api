package io.github.alexistrejo11.bank.accounts.infrastructure.event;

import io.github.alexistrejo11.bank.accounts.application.command.PostTransferToLedgerCommand;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.accounts.application.PostTransferToLedgerUseCase;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies ledger postings when the payments module publishes
 * {@link TransferCompletedEvent}.
 */
@Component
public class AccountsTransferListener {

	private final PostTransferToLedgerUseCase postTransferToLedger;

	public AccountsTransferListener(PostTransferToLedgerUseCase postTransferToLedger) {
		this.postTransferToLedger = postTransferToLedger;
	}

	@EventListener
	@Transactional
	public void onTransferCompleted(TransferCompletedEvent event) {
		postTransferToLedger.execute(new PostTransferToLedgerCommand(
				event.sourceAccountId(),
				event.targetAccountId(),
				event.amount(),
				event.currencyCode(),
				"TRANSFER",
				event.transferId().value()));
	}
}
