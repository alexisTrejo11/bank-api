package io.github.alexistrejo11.bank.audit.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.audit.application.AuditRecordQuery;
import io.github.alexistrejo11.bank.audit.infrastructure.persistence.entity.AuditRecordEntity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class AuditRecordSpecifications {

	private AuditRecordSpecifications() {
	}

	public static Specification<AuditRecordEntity> matching(AuditRecordQuery query) {
		return (root, cq, cb) -> {
			List<Predicate> parts = new ArrayList<>();
			if (query.eventType() != null && !query.eventType().isBlank()) {
				parts.add(cb.equal(root.get("eventType"), query.eventType()));
			}
			if (query.actorId() != null) {
				parts.add(cb.equal(root.get("actorId"), query.actorId()));
			}
			if (query.entityType() != null && !query.entityType().isBlank()) {
				parts.add(cb.equal(root.get("entityType"), query.entityType()));
			}
			if (query.entityId() != null) {
				parts.add(cb.equal(root.get("entityId"), query.entityId()));
			}
			if (query.from() != null) {
				parts.add(cb.greaterThanOrEqualTo(root.get("createdAt"), query.from()));
			}
			if (query.to() != null) {
				parts.add(cb.lessThanOrEqualTo(root.get("createdAt"), query.to()));
			}
			return parts.isEmpty() ? cb.conjunction() : cb.and(parts.toArray(Predicate[]::new));
		};
	}
}
