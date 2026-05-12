package io.github.alexistrejo11.bank.payments.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.alexistrejo11.bank.payments.application.command.InitiateTransferCommand;
import io.github.alexistrejo11.bank.payments.application.idempotency.TransferIdempotencyCache;
import io.github.alexistrejo11.bank.payments.domain.repository.AccountLedgerInfoPort;
import io.github.alexistrejo11.bank.payments.domain.repository.AccountLedgerInfoPort.AccountLedgerInfo;
import io.github.alexistrejo11.bank.payments.domain.repository.TransferIdempotencyPort;
import io.github.alexistrejo11.bank.payments.domain.repository.TransferRepository;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.AccountId;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import io.github.alexistrejo11.bank.shared.shared_kernel.result.Result;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.TransferCompletedEvent;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class InitiateTransferHandlerTest {

	@Mock
	TransferRepository transferRepository;

	@Mock
	AccountLedgerInfoPort accountLedgerInfoPort;

	@Mock
	TransferIdempotencyPort idempotencyPort;

	@Mock
	ApplicationEventPublisher eventPublisher;

	InitiateTransferHandler handler;

	final UserId userId = UserId.random();
	final UUID idem = UUID.randomUUID();
	final UUID src = UUID.randomUUID();
	final UUID tgt = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		var cache = new TransferIdempotencyCache(idempotencyPort);
		handler = new InitiateTransferHandler(transferRepository, accountLedgerInfoPort, cache, eventPublisher);
	}

	@Test
	void should_fail_when_source_not_owned() {
		when(idempotencyPort.getCachedJson(any(), any())).thenReturn(Optional.empty());
		when(transferRepository.findByUserIdAndIdempotencyKey(any(), any())).thenReturn(Optional.empty());
		when(accountLedgerInfoPort.find(AccountId.of(src)))
				.thenReturn(Optional.of(new AccountLedgerInfo(src, UserId.random(), "USD", new BigDecimal("100"), true)));

		Result<?> r = handler.handle(new InitiateTransferCommand(userId, idem, src, tgt, new BigDecimal("10"), "USD"));
		assertThat(r.isFailure()).isTrue();
		verify(transferRepository, never()).save(any());
	}

	@Test
	void should_publish_completed_event_on_success() {
		when(idempotencyPort.getCachedJson(any(), any())).thenReturn(Optional.empty());
		when(transferRepository.findByUserIdAndIdempotencyKey(any(), any())).thenReturn(Optional.empty());
		when(accountLedgerInfoPort.find(AccountId.of(src)))
				.thenReturn(Optional.of(new AccountLedgerInfo(src, userId, "USD", new BigDecimal("100"), true)));
		when(accountLedgerInfoPort.find(AccountId.of(tgt)))
				.thenReturn(Optional.of(new AccountLedgerInfo(tgt, UserId.random(), "USD", BigDecimal.ZERO, true)));
		when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		Result<?> r = handler.handle(new InitiateTransferCommand(userId, idem, src, tgt, new BigDecimal("10"), "USD"));
		assertThat(r.isSuccess()).isTrue();
		ArgumentCaptor<TransferCompletedEvent> cap = ArgumentCaptor.forClass(TransferCompletedEvent.class);
		verify(eventPublisher).publishEvent(cap.capture());
		assertThat(cap.getValue().amount()).isEqualByComparingTo("10");
	}
}
