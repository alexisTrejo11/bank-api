package io.github.alexistrejo11.bank.payments.infrastructure.event;

import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferFailedEvent;
import io.github.alexistrejo11.bank.shared.event.TransferReversedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Stub until the notifications module exists: logs transfer lifecycle for ops visibility.
 */
@Component
public class PaymentsNotificationListener {

	private static final Logger log = LoggerFactory.getLogger(PaymentsNotificationListener.class);

	@EventListener
	public void onCompleted(TransferCompletedEvent event) {
		log.info("notify_transfer_completed transferId={} source={} target={} amount={} {}",
				event.transferId().value(),
				event.sourceAccountId().value(),
				event.targetAccountId().value(),
				event.amount(),
				event.currencyCode());
	}

	@EventListener
	public void onFailed(TransferFailedEvent event) {
		log.info("notify_transfer_failed transferId={} code={} message={}",
				event.transferId().value(),
				event.reasonCode(),
				event.message());
	}

	@EventListener
	public void onReversed(TransferReversedEvent event) {
		log.info("notify_transfer_reversed reversalId={} originalId={} amount={} {}",
				event.reversalTransferId().value(),
				event.originalTransferId().value(),
				event.amount(),
				event.currencyCode());
	}
}
