package io.github.alexistrejo11.bank.notifications.application;

import io.github.alexistrejo11.bank.notifications.api.dto.response.NotificationRecordResponse;
import io.github.alexistrejo11.bank.notifications.api.dto.response.NotificationStatusCountResponse;
import io.github.alexistrejo11.bank.notifications.api.dto.response.NotificationSummaryResponse;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationChannel;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationStatus;
import io.github.alexistrejo11.bank.notifications.infrastructure.persistence.entity.NotificationEntity;
import io.github.alexistrejo11.bank.notifications.infrastructure.persistence.repository.NotificationJpaRepository;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationMonitoringService {

	private final NotificationJpaRepository notificationJpaRepository;

	public NotificationMonitoringService(NotificationJpaRepository notificationJpaRepository) {
		this.notificationJpaRepository = notificationJpaRepository;
	}

	@Transactional(readOnly = true)
	public Page<NotificationRecordResponse> list(NotificationStatus status, NotificationChannel channel, Pageable pageable) {
		Page<NotificationEntity> page;
		if (status != null && channel != null) {
			page = notificationJpaRepository.findByStatusAndChannel(status, channel, pageable);
		}
		else if (status != null) {
			page = notificationJpaRepository.findByStatus(status, pageable);
		}
		else if (channel != null) {
			page = notificationJpaRepository.findByChannel(channel, pageable);
		}
		else {
			page = notificationJpaRepository.findAll(pageable);
		}
		return page.map(NotificationMonitoringService::toRecord);
	}

	@Transactional(readOnly = true)
	public NotificationSummaryResponse summary() {
		Map<NotificationStatus, Long> counts = new EnumMap<>(NotificationStatus.class);
		for (NotificationStatus s : NotificationStatus.values()) {
			counts.put(s, 0L);
		}
		for (Object[] row : notificationJpaRepository.countGroupedByStatus()) {
			NotificationStatus st = (NotificationStatus) row[0];
			long cnt = ((Number) row[1]).longValue();
			counts.put(st, cnt);
		}
		long total = counts.values().stream().mapToLong(Long::longValue).sum();
		List<NotificationStatusCountResponse> byStatus = counts.entrySet().stream()
				.map(e -> new NotificationStatusCountResponse(e.getKey().name(), e.getValue()))
				.toList();
		return new NotificationSummaryResponse(total, byStatus);
	}

	private static NotificationRecordResponse toRecord(NotificationEntity e) {
		return new NotificationRecordResponse(
				e.getId(),
				e.getUserId(),
				e.getChannel().name(),
				e.getTemplateKey(),
				e.getStatus().name(),
				e.getSourceEventType(),
				e.getSubject(),
				e.getRecipientHint(),
				e.getCreatedAt(),
				e.getDispatchedAt(),
				e.getErrorMessage()
		);
	}
}
