package io.github.alexistrejo11.bank.audit.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.audit.infrastructure.persistence.entity.AuditRecordEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditRecordJpaRepository extends JpaRepository<AuditRecordEntity, UUID>, JpaSpecificationExecutor<AuditRecordEntity> {
}
