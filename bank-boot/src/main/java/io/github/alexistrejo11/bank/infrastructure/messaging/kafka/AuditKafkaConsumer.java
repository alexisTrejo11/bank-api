package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.f4b6a3.uuid.UuidCreator;
import io.github.alexistrejo11.bank.audit.application.handler.command.AppendAuditRecordHandler;
import io.github.alexistrejo11.bank.audit.domain.command.AppendAuditRecordCommand;
import io.github.alexistrejo11.bank.audit.domain.service.AuditDomainEventMapper;
import io.github.alexistrejo11.bank.audit.domain.service.AuditDomainEventMapper.EntityRef;
import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;
import io.github.alexistrejo11.bank.shared.messaging.BankKafkaTopics;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Append-only audit from Kafka (no SecurityContext; actorId is null for automated pipeline).
 */
@Component
@ConditionalOnProperty(prefix = "bank.kafka", name = "enabled", havingValue = "true")
public class AuditKafkaConsumer {

	private static final Logger log = LoggerFactory.getLogger(AuditKafkaConsumer.class);
	private static final Logger auditChannel = LoggerFactory.getLogger("AUDIT");

	private final AppendAuditRecordHandler appendAuditRecordHandler;
	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	public AuditKafkaConsumer(AppendAuditRecordHandler appendAuditRecordHandler) {
		this.appendAuditRecordHandler = appendAuditRecordHandler;
	}

	@KafkaListener(
			topics = { BankKafkaTopics.TRANSFERS, BankKafkaTopics.LOANS },
			groupId = "audit-cg",
			containerFactory = "bankDomainEventKafkaListenerContainerFactory"
	)
	public void onEvent(BankDomainEvent event) {
		EntityRef ref = AuditDomainEventMapper.entityRef(event);
		String eventType = AuditDomainEventMapper.eventType(event);
		String payload;
		try {
			payload = objectMapper.writeValueAsString(event);
		}
		catch (JsonProcessingException e) {
			log.error("audit_kafka_serialize_failed eventType={} eventId={}", eventType, event.eventId(), e);
			payload = "{\"error\":\"serialization_failed\",\"eventId\":\"" + event.eventId() + "\"}";
		}
		UUID id = UuidCreator.getTimeOrderedEpoch();
		Instant createdAt = Instant.now();
		auditChannel.info(
				"eventCategory=AUDIT source=kafka eventType={} entityType={} entityId={} eventId={}",
				eventType,
				ref.entityType(),
				ref.entityId(),
				event.eventId()
		);
		appendAuditRecordHandler.handle(new AppendAuditRecordCommand(
				id,
				eventType,
				null,
				ref.entityType(),
				ref.entityId(),
				payload,
				createdAt
		));
	}
}
