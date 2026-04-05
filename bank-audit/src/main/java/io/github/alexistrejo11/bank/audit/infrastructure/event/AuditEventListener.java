package io.github.alexistrejo11.bank.audit.infrastructure.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.alexistrejo11.bank.audit.application.AuditDomainEventMapper;
import io.github.alexistrejo11.bank.audit.application.AuditDomainEventMapper.EntityRef;
import io.github.alexistrejo11.bank.audit.infrastructure.persistence.entity.AuditRecordEntity;
import io.github.alexistrejo11.bank.audit.infrastructure.persistence.repository.AuditRecordJpaRepository;
import io.github.alexistrejo11.bank.iam.infrastructure.security.IamUserPrincipal;
import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;
import com.github.f4b6a3.uuid.UuidCreator;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuditEventListener {

	private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

	private final AuditRecordJpaRepository auditRecordRepository;
	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	public AuditEventListener(AuditRecordJpaRepository auditRecordRepository) {
		this.auditRecordRepository = auditRecordRepository;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void onBankDomainEvent(BankDomainEvent event) {
		EntityRef ref = AuditDomainEventMapper.entityRef(event);
		String eventType = AuditDomainEventMapper.eventType(event);
		String payload;
		try {
			payload = objectMapper.writeValueAsString(event);
		}
		catch (JsonProcessingException e) {
			log.error("audit_serialize_failed eventType={} eventId={}", eventType, event.eventId(), e);
			payload = "{\"error\":\"serialization_failed\",\"eventId\":\"" + event.eventId() + "\"}";
		}
		UUID id = UuidCreator.getTimeOrderedEpoch();
		Instant createdAt = Instant.now();
		UUID actorId = currentActorId();
		auditRecordRepository.save(new AuditRecordEntity(
				id,
				eventType,
				actorId,
				ref.entityType(),
				ref.entityId(),
				payload,
				createdAt
		));
	}

	private static UUID currentActorId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return null;
		}
		if (auth.getPrincipal() instanceof IamUserPrincipal p) {
			return p.userId().value();
		}
		return null;
	}
}
