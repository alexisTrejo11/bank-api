package io.github.alexistrejo11.bank.notifications.infrastructure.persistence.adapter;

import io.github.alexistrejo11.bank.notifications.domain.model.NotificationLogRecord;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationRecordFilters;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationStatus;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationSummaryModel;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationLogRepository;
import io.github.alexistrejo11.bank.notifications.infrastructure.persistence.entity.NotificationEntity;
import io.github.alexistrejo11.bank.notifications.infrastructure.persistence.repository.NotificationJpaRepository;
import io.github.alexistrejo11.bank.shared.page.PageResult;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class NotificationLogRepositoryAdapter implements NotificationLogRepository {

	private final NotificationJpaRepository jpaRepository;

	public NotificationLogRepositoryAdapter(NotificationJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public NotificationLogRecord save(NotificationLogRecord record) {
		return toRecord(jpaRepository.save(toEntity(record)));
	}

	@Override
	public PageResult<NotificationLogRecord> search(NotificationRecordFilters filters, int page, int size) {
		var pageable = PageRequest.of(page, size);
		Page<NotificationEntity> springPage;
		if (filters.status() != null && filters.channel() != null) {
			springPage = jpaRepository.findByStatusAndChannel(filters.status(), filters.channel(), pageable);
		}
		else if (filters.status() != null) {
			springPage = jpaRepository.findByStatus(filters.status(), pageable);
		}
		else if (filters.channel() != null) {
			springPage = jpaRepository.findByChannel(filters.channel(), pageable);
		}
		else {
			springPage = jpaRepository.findAll(pageable);
		}
		return new PageResult<>(
				springPage.getContent().stream().map(this::toRecord).toList(),
				springPage.getTotalElements(),
				springPage.getNumber(),
				springPage.getSize()
		);
	}

	@Override
	public NotificationSummaryModel summaryByStatus() {
		Map<NotificationStatus, Long> counts = new EnumMap<>(NotificationStatus.class);
		for (NotificationStatus s : NotificationStatus.values()) {
			counts.put(s, 0L);
		}
		for (Object[] row : jpaRepository.countGroupedByStatus()) {
			NotificationStatus st = (NotificationStatus) row[0];
			long cnt = ((Number) row[1]).longValue();
			counts.put(st, cnt);
		}
		long total = counts.values().stream().mapToLong(Long::longValue).sum();
		List<NotificationSummaryModel.NotificationStatusCount> byStatus = counts.entrySet().stream()
				.map(e -> new NotificationSummaryModel.NotificationStatusCount(e.getKey(), e.getValue()))
				.toList();
		return new NotificationSummaryModel(total, byStatus);
	}

	private NotificationEntity toEntity(NotificationLogRecord r) {
		return new NotificationEntity(
				r.id(),
				r.userId(),
				r.channel(),
				r.templateKey(),
				r.status(),
				r.sourceEventType(),
				r.subject(),
				r.bodyHtml(),
				r.recipientHint(),
				r.metadataJson(),
				r.errorMessage(),
				r.createdAt(),
				r.updatedAt(),
				r.dispatchedAt()
		);
	}

	private NotificationLogRecord toRecord(NotificationEntity e) {
		return new NotificationLogRecord(
				e.getId(),
				e.getUserId(),
				e.getChannel(),
				e.getTemplateKey(),
				e.getStatus(),
				e.getSourceEventType(),
				e.getSubject(),
				e.getBodyHtml(),
				e.getRecipientHint(),
				e.getMetadataJson(),
				e.getErrorMessage(),
				e.getCreatedAt(),
				e.getUpdatedAt(),
				e.getDispatchedAt()
		);
	}
}
