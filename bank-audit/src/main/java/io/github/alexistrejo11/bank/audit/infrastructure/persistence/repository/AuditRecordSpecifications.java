package io.github.alexistrejo11.bank.audit.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.audit.domain.model.AuditRecordFilters;
import io.github.alexistrejo11.bank.audit.infrastructure.persistence.entity.AuditRecordEntity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class AuditRecordSpecifications {

	private AuditRecordSpecifications() {
	}

	public static Specification<AuditRecordEntity> matching(AuditRecordFilters filters) {
		return (root, cq, cb) -> {
			List<Predicate> parts = new ArrayList<>();
			if (filters.eventType() != null && !filters.eventType().isBlank()) {
				parts.add(cb.equal(root.get("eventType"), filters.eventType()));
			}
			if (filters.actorId() != null) {
				parts.add(cb.equal(root.get("actorId"), filters.actorId()));
			}
			if (filters.entityType() != null && !filters.entityType().isBlank()) {
				parts.add(cb.equal(root.get("entityType"), filters.entityType()));
			}
			if (filters.entityId() != null) {
				parts.add(cb.equal(root.get("entityId"), filters.entityId()));
			}
			if (filters.from() != null) {
				parts.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filters.from()));
			}
			if (filters.to() != null) {
				parts.add(cb.lessThanOrEqualTo(root.get("createdAt"), filters.to()));
			}
			return parts.isEmpty() ? cb.conjunction() : cb.and(parts.toArray(Predicate[]::new));
		};
	}
}
