package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import io.github.alexistrejo11.bank.accounts.domain.command.PostTransferToLedgerCommand;
import io.github.alexistrejo11.bank.accounts.domain.port.in.command.PostTransferToLedgerUseCase;
import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;
import io.github.alexistrejo11.bank.shared.event.LoanDisbursedEvent;
import io.github.alexistrejo11.bank.shared.event.LoanRepaymentCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.messaging.BankKafkaTopics;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Posts ledger entries from transfer and loan streams (idempotency via referenceId = eventId).
 */
@Component
@ConditionalOnProperty(prefix = "bank.kafka", name = "enabled", havingValue = "true")
public class AccountsKafkaConsumer {

	private static final Logger log = LoggerFactory.getLogger(AccountsKafkaConsumer.class);

	private final PostTransferToLedgerUseCase postTransferToLedger;
	private final UUID internalFundingAccountId;

	public AccountsKafkaConsumer(
			PostTransferToLedgerUseCase postTransferToLedger,
			@Value("${bank.loans.internal-funding-account-id}") UUID internalFundingAccountId
	) {
		this.postTransferToLedger = postTransferToLedger;
		this.internalFundingAccountId = internalFundingAccountId;
	}

	@KafkaListener(
			topics = { BankKafkaTopics.TRANSFERS, BankKafkaTopics.LOANS },
			groupId = "accounts-cg",
			containerFactory = "bankDomainEventKafkaListenerContainerFactory"
	)
	public void onDomainEvent(BankDomainEvent event) {
		switch (event) {
			case TransferCompletedEvent e -> postTransferToLedger.execute(new PostTransferToLedgerCommand(
					e.sourceAccountId(),
					e.targetAccountId(),
					e.amount(),
					e.currencyCode(),
					"TRANSFER",
					e.eventId()
			));
			case LoanDisbursedEvent e -> postTransferToLedger.execute(new PostTransferToLedgerCommand(
					AccountId.of(internalFundingAccountId),
					e.checkingAccountId(),
					e.amount(),
					e.currencyCode(),
					"LOAN_DISBURSE",
					e.eventId()
			));
			case LoanRepaymentCompletedEvent e -> postTransferToLedger.execute(new PostTransferToLedgerCommand(
					e.checkingAccountId(),
					AccountId.of(internalFundingAccountId),
					e.amount(),
					e.currencyCode(),
					"LOAN_REPAYMENT",
					e.eventId()
			));
			default -> log.trace("accounts_cg_skip eventType={}", event.getClass().getSimpleName());
		}
	}
}
