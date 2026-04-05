package io.github.alexistrejo11.bank.payments.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.alexistrejo11.bank.payments.domain.port.out.AccountLedgerInfoPort;
import io.github.alexistrejo11.bank.payments.domain.port.out.AccountLedgerInfoPort.AccountLedgerInfo;
import io.github.alexistrejo11.bank.payments.domain.port.out.TransferIdempotencyPort;
import io.github.alexistrejo11.bank.payments.infrastructure.persistence.entity.TransferEntity;
import io.github.alexistrejo11.bank.payments.infrastructure.persistence.repository.TransferJpaRepository;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import io.github.alexistrejo11.bank.shared.result.Result;
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
class TransferApplicationServiceTest {

	@Mock
	TransferJpaRepository transferRepository;

	@Mock
	AccountLedgerInfoPort accountLedgerInfoPort;

	@Mock
	TransferIdempotencyPort idempotencyPort;

	@Mock
	ApplicationEventPublisher eventPublisher;

	TransferApplicationService service;

	final UserId userId = UserId.random();
	final UUID idem = UUID.randomUUID();
	final UUID src = UUID.randomUUID();
	final UUID tgt = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		service = new TransferApplicationService(transferRepository, accountLedgerInfoPort, idempotencyPort, eventPublisher);
	}

	@Test
	void should_fail_when_source_not_owned() {
		when(idempotencyPort.getCachedJson(any(), any())).thenReturn(Optional.empty());
		when(transferRepository.findByUserIdAndIdempotencyKey(any(), any())).thenReturn(Optional.empty());
		when(accountLedgerInfoPort.find(AccountId.of(src)))
				.thenReturn(Optional.of(new AccountLedgerInfo(src, UserId.random(), "USD", new BigDecimal("100"), true)));

		Result<?> r = service.initiate(userId, idem, src, tgt, new BigDecimal("10"), "USD");
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
		when(transferRepository.save(any(TransferEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		Result<?> r = service.initiate(userId, idem, src, tgt, new BigDecimal("10"), "USD");
		assertThat(r.isSuccess()).isTrue();
		ArgumentCaptor<TransferCompletedEvent> cap = ArgumentCaptor.forClass(TransferCompletedEvent.class);
		verify(eventPublisher).publishEvent(cap.capture());
		assertThat(cap.getValue().amount()).isEqualByComparingTo("10");
	}
}
