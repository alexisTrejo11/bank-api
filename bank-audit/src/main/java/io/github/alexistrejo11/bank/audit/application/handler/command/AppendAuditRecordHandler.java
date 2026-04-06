package io.github.alexistrejo11.bank.audit.application.handler.command;

import io.github.alexistrejo11.bank.audit.domain.command.AppendAuditRecordCommand;
import io.github.alexistrejo11.bank.audit.domain.model.AuditRecord;
import io.github.alexistrejo11.bank.audit.domain.port.out.AuditRecordRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AppendAuditRecordHandler {

	private final AuditRecordRepository auditRecordRepository;

	public AppendAuditRecordHandler(AuditRecordRepository auditRecordRepository) {
		this.auditRecordRepository = auditRecordRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(AppendAuditRecordCommand command) {
		auditRecordRepository.append(new AuditRecord(
				command.id(),
				command.eventType(),
				command.actorId(),
				command.entityType(),
				command.entityId(),
				command.payloadJson(),
				command.createdAt()
		));
	}
}
