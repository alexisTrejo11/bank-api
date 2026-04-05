package io.github.alexistrejo11.bank.notifications.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.notifications.domain.model.NotificationChannel;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationStatus;
import io.github.alexistrejo11.bank.notifications.infrastructure.persistence.entity.NotificationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {

	Page<NotificationEntity> findByStatus(NotificationStatus status, Pageable pageable);

	Page<NotificationEntity> findByChannel(NotificationChannel channel, Pageable pageable);

	Page<NotificationEntity> findByStatusAndChannel(NotificationStatus status, NotificationChannel channel, Pageable pageable);

	@Query("select n.status, count(n) from NotificationEntity n group by n.status")
	List<Object[]> countGroupedByStatus();
}
