package io.github.alexistrejo11.bank.audit.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.TransferId;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuditDomainEventMapperTest {

	@Test
	@DisplayName("should map TransferCompletedEvent to Transfer entity ref")
	void should_map_transfer_completed_event() {
		UUID tid = UUID.randomUUID();
		var event = new TransferCompletedEvent(
				TransferId.of(tid),
				AccountId.random(),
				AccountId.random(),
				new BigDecimal("1.00"),
				"USD"
		);
		assertThat(AuditDomainEventMapper.eventType(event)).isEqualTo("TransferCompletedEvent");
		var ref = AuditDomainEventMapper.entityRef(event);
		assertThat(ref.entityType()).isEqualTo("Transfer");
		assertThat(ref.entityId()).isEqualTo(tid);
	}
}
